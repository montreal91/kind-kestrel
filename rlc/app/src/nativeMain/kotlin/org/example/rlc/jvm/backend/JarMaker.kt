package org.example.rlc.jvm.backend

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import org.example.rlc.jvm.ir.loxMainClassName
import org.example.rlc.jvm.ir.toUtf8Value
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

/**
 * This file contains sorta bad code, but its only purpose is to put bytecode in a jar file.
 *
 * It is totally internal and isolated from the other parts of compiler,
 * so it doesn't really matter.
 * I'll improve it if I find enough time and motivation.
 */

@OptIn(ExperimentalForeignApi::class)
internal fun createJarFile(classFiles: List<CompiledBinaryFile>, outputJar: String) {

  memScoped {
    val systemFile = fopen(outputJar, _Mode = "wb") ?: error("Failed to open file for writing")
    try {
      val jarFile = buildJarFile(classFiles)
      writeByteArrayToFile(systemFile, jarFile.toBytes())
    } finally {
      fclose(systemFile)
    }
  }
}

private val manifesto = """
        Manifest-Version: 1.0
        Main-Class: $loxMainClassName

        """.trimIndent()

private fun buildJarFile(classFiles: List<CompiledBinaryFile>): JarFile {
  val jarFile = JarFile()
  for (file in classFiles) {
    jarFile.addEntry(compiledFileToJarEntry(file))
  }

  return jarFile
}

@OptIn(ExperimentalForeignApi::class)
private fun writeByteArrayToFile(file: CPointer<FILE>, bytes: ByteArray) {
  val written = fwrite(bytes.refTo(index = 0), _Size = 1u, bytes.size.toULong(), file)
  if (written != bytes.size.toULong()) {
    throw IllegalStateException("Failed to write all data to file")
  }
}

private class JarEntry(
  val name: String,
  val content: List<Byte>,
  val crc32: UInt,
  val fileSize: Int
) {
  val entrySize: Int get() = content.size
}

private class JarFile {
  private val entryOffsetTable = mutableMapOf<String, Int>()
  private val entryList = mutableListOf<JarEntry>()
  private var lastOffset = 0

  init {
    addEntry(makeDir(dirName = "META-INF/"))
    addEntry(stringToJarEntry(fileName = "META-INF/MANIFEST.MF", content = manifesto))
  }

  fun addEntry(entry: JarEntry) {
    if (entryOffsetTable.containsKey(entry.name)) {
      return
    }

    entryOffsetTable[entry.name] = lastOffset
    entryList.add(entry)
    lastOffset += entry.entrySize
  }

  fun toBytes(): ByteArray {
    val res = mutableListOf<Byte>()
    val centralDirectory = mutableListOf<Byte>()

    for (entry in entryList) {
      res.addAll(entry.content)
      centralDirectory.addAll(getCentralDirectoryFileHeader(entry))
    }

    res.addAll(centralDirectory)
    res.addAll(makeEndOfCentralDirectoryRecord(centralDirectory.size, lastOffset))
    return res.toByteArray()
  }

  private fun makeEndOfCentralDirectoryRecord(
    centralDirectorySize: Int,
    centralDirectoryOffset: Int
  ): List<Byte> {
    val res = mutableListOf<Byte>()

    // Signature
    res.add(0x50)
    res.add(0x4b)
    res.add(0x05)
    res.add(0x06)

    // Number of this disk
    res.addAll(0.toShort().toBytes())

    // Disk where central directory starts
    res.addAll(0.toShort().toBytes())

    // Number of central directory records on this disk (short)
    val numberOfRecords = entryList.size.toShort()
    res.addAll(numberOfRecords.toBytes().reversed())

    // Total number of central directory records (short)
    res.addAll(numberOfRecords.toBytes().reversed())

    // Size of central directory (int)
    res.addAll(centralDirectorySize.toBytes().reversed())

    // Offset of start of central directory, relative to the start of archive
    res.addAll(centralDirectoryOffset.toBytes().reversed())

    // Comment length (short)
    res.addAll(0.toShort().toBytes())

    return res.toList()
  }

  private fun getCentralDirectoryFileHeader(entry: JarEntry): List<Byte> {
    val bytes = mutableListOf<Byte>()
    val signature = 0x02014B50
    bytes.addAll(signature.toBytes().reversed())

    val versionMadeBy: Short = 0x003F
    bytes.addAll(versionMadeBy.toBytes().reversed())

    val minimumVersion: Short = 0x000A
    bytes.addAll(minimumVersion.toBytes().reversed())

    val generalPurposeFlag: Short = 0x0000
    bytes.addAll(generalPurposeFlag.toBytes())

    val compressionMethod: Short = 0x0000
    bytes.addAll(compressionMethod.toBytes())

    // Nah, don't need this stuff anyway.
    val fileLastModificationTime: Short = 0
    val fileLastModificationDate: Short = 0
    bytes.addAll(fileLastModificationTime.toBytes())
    bytes.addAll(fileLastModificationDate.toBytes())

    bytes.addAll(entry.crc32.toBytes().reversed())

    // compressed size
    bytes.addAll(entry.fileSize.toBytes().reversed())

    // uncompressed size
    bytes.addAll(entry.fileSize.toBytes().reversed())

    // name length
    val nameLength: Short = entry.name.length.toShort()
    bytes.addAll(nameLength.toBytes().reversed())

    val extraFieldLength: Short = 0
    bytes.addAll(extraFieldLength.toBytes())

    val fileCommentLength: Short = 0
    bytes.addAll(fileCommentLength.toBytes())

    val diskNumber: Short = 0
    bytes.addAll(diskNumber.toBytes())

    val internalFileAttributes: Short = 0
    bytes.addAll(internalFileAttributes.toBytes())

    val externalFileAttributes = 0
    bytes.addAll(externalFileAttributes.toBytes())

    if (!entryOffsetTable.containsKey(entry.name)) {
      throw IllegalStateException(
        "Jar Archive does not contain file with name: [${entry.name}]"
      )
    }

    val offset: Int = this.entryOffsetTable[entry.name]!!
    bytes.addAll(offset.toBytes().reversed())

    // File name
    bytes.addAll(entry.name.toUtf8Value().value.toList())

    return bytes
  }
}

private fun compiledFileToJarEntry(binaryFile: CompiledBinaryFile) = createJarEntry(
  binaryFile.fileName, binaryFile.binary
)

private fun createJarEntry(fileName: String, content: List<Byte>) : JarEntry {
  val bytes = mutableListOf<Byte>()
  val signature = 0x04034b50
  bytes.addAll(signature.toBytes().reversed())

  val minVersion: Short = 0x000A
  bytes.addAll(minVersion.toBytes().reversed())

  val generalPurposeBitFlag: Short = 0x0000
  bytes.addAll(generalPurposeBitFlag.toBytes().reversed())

  val compressionMethod: Short = 0
  bytes.addAll(compressionMethod.toBytes().reversed())

  // Nah, don't need this stuff anyway.
  val fileLastModificationTime: Short = 0
  val fileLastModificationDate: Short = 0
  bytes.addAll(fileLastModificationTime.toBytes())
  bytes.addAll(fileLastModificationDate.toBytes())

  val crc32Uncompressed = crc32(content.toByteArray())
  bytes.addAll(crc32Uncompressed.toBytes().reversed())

  val compressedSize = content.size
  val uncompressedSize = content.size

  bytes.addAll(compressedSize.toBytes().reversed())
  bytes.addAll(uncompressedSize.toBytes().reversed())

  val fileNameLength = fileName.length.toShort()
  bytes.addAll(fileNameLength.toBytes().reversed())

  val extraFieldLength: Short = 0
  bytes.addAll(extraFieldLength.toBytes())

  val fileNameBytes = fileName.encodeToByteArray()
  bytes.addAll(fileNameBytes.asList())

  // Actual file contents
  bytes.addAll(content)

  return JarEntry(
    name = fileName,
    content = bytes.toList(),
    crc32 = crc32Uncompressed,
    fileSize = uncompressedSize
  )
}

private fun crc32(data: ByteArray): UInt {
  val table = UIntArray(size = 256) { i ->
    var crc = i.toUInt()
    repeat(times = 8) {
      crc = if (crc and 1u != 0u) {
        (crc ushr 1) xor 0xEDB88320u
      } else {
        crc ushr 1
      }
    }
    crc
  }

  var crc = 0xFFFFFFFFu
  for (byte in data) {
    val index = (crc xor byte.toUInt()) and 0xFFu
    crc = (crc ushr 8) xor table[index.toInt()]
  }
  return crc xor 0xFFFFFFFFu
}

private infix fun UInt.ushr(bits: Int): UInt {
  return (this.toInt() ushr bits).toUInt()
}

private fun makeDir(dirName: String) = createJarEntry(dirName, listOf())

private fun stringToJarEntry(fileName: String, content: String) = createJarEntry(
  fileName, content.encodeToByteArray().toList()
)
