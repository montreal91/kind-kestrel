package org.example.rlc.jvm.backend

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import org.example.rlc.jvm.ir.ClassFile
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
private fun writeJarEntry(file: CPointer<FILE>, entryName: String, content: ByteArray) {
  val header = ByteArray(30)
  header[0] = 0x50
  header[1] = 0x4B
  header[2] = 0x03
  header[3] = 0x04
  header[10] = 0x08 // Compression method (deflate)
  val nameBytes = entryName.encodeToByteArray()
  val nameLength = nameBytes.size.toShort()
  val contentLength = content.size

  // Write local file header
//  header[26] = (nameLength and 0xFF).toByte()
  header[26] = nameLength.toBytes()[0]
//  header[27] = ((nameLength shr 8) and 0xFF).toByte()
  header[27] = nameLength.toBytes()[1]
  header[18] = (contentLength and 0xFF).toByte()
  header[19] = ((contentLength shr 8) and 0xFF).toByte()
  header[20] = ((contentLength shr 16) and 0xFF).toByte()
  header[21] = ((contentLength shr 24) and 0xFF).toByte()

  fwrite(header.refTo(0), 1u, header.size.toULong(), file)
  fwrite(nameBytes.refTo(0), 1u, nameBytes.size.toULong(), file)
  fwrite(content.refTo(0), 1u, content.size.toULong(), file)
}

@OptIn(ExperimentalForeignApi::class)
fun createJarFile(classFiles: List<ClassFile>, outputJar: String, mainClass: String) {
  val manifestContent = """
        Manifest-Version: 1.0
        Main-Class: $mainClass
        """.trimIndent()

  memScoped {
    val jarFile = fopen(outputJar, "wb") ?: error("Failed to open file for writing")
    try {
      // Write manifest entry
//      writeJarEntry(jarFile, "META-INF/MANIFEST.MF", manifestContent.encodeToByteArray())

      // Write class files
      classFiles.forEach { classFile ->
        try {
//          val classFileName = classFilePath.substringAfterLast("/")
          val classFileBytes = ClassCompiler().compileClass(classFile)
          writeJarEntry(jarFile, classFile.filename, classFileBytes)
        }
        catch (e: Throwable) {
          // do nothing
        }
      }
    } finally {
      fclose(jarFile)
    }
  }
}
