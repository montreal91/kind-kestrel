from utils import run_lox


def test_missing_arg(lox, line_ending):
    res = run_lox(lox, "print/lox/missing_arg.lox")

    assert res.return_code == 65
    assert res.stderr == f"[line 2] Error at ';': Expect expression.{line_ending}"


def test_string(lox, line_ending):
    res = run_lox(lox, "print/lox/string.lox")

    assert res.return_code == 0
    assert res.stdout == f"Hello, Lox{line_ending}"
