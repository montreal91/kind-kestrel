import os

import pytest


def pytest_addoption(parser):
    parser.addoption("--lox", action="store", default="lox")


def pytest_generate_tests(metafunc):
    option_value = metafunc.config.option.lox
    if "lox" in metafunc.fixturenames and option_value is not None:
        metafunc.parametrize("lox", [option_value])


@pytest.fixture
def line_ending():
    if os.name == "nt":
        return "\r\n"
    return "\n"
