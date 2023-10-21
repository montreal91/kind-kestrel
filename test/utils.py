import subprocess

from typing import NamedTuple


class LoxCommandResult(NamedTuple):
    stdout: str
    stderr: str
    return_code: int


def run_lox(lox, file):
    cmd = f"{lox} {file}"
    process = subprocess.Popen(
        cmd,
        shell=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    process.wait(timeout=5)

    out = "\n".join([line.decode() for line in process.stdout])
    err = "\n".join([line.decode() for line in process.stderr])

    return LoxCommandResult(
        stdout=out, stderr=err, return_code=process.returncode
    )
