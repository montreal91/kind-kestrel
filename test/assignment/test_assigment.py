from utils import run_lox


def test_associativity(lox, line_ending):
    res = run_lox(lox, "assignment/lox/associativity.lox")
    # capture = capsys.readouterr().out
    assert res.return_code == 0
    assert res.stdout == "".join([f"c{line_ending}"] * 3)
