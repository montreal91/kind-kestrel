import subprocess

from typing import NamedTuple


class LoxCommandResult(NamedTuple):
    stdout: str
    stderr: str
    return_code: int


def run_lox(lox, file):
    cmd = f"{lox} {file}"
    process = subprocess.run(
        cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE
    )
    out = process.stdout.decode()
    err = process.stderr.decode()

    return LoxCommandResult(
        stdout=out, stderr=err, return_code=process.returncode
    )
