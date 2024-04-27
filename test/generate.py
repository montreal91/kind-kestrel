import os


ASSERTION_MESSAGE = (
    "            f\"Expected: {expected_output.strip()}, "
    "Actual: {actual_output.strip()}\"\n"
)

EXIT_CODE_ASSERTION_MESSAGE = "f\"Expected exit code {expected_exit_code}, but actual was {res.exit_code}\""

IGNORED_FOLDERS = (
    "benchmark",
    "expressions",
    "scanning",
)


def generate_test_cases(test_dir, output_file):
    with open(output_file, "w", encoding="utf-8") as f:
        f.write("# Generated test cases\n\n")
        f.write("from utils import run_lox\n\n\n")
        for feature_dir in os.listdir(test_dir):
            if feature_dir in IGNORED_FOLDERS:
                continue

            feature_path = os.path.join(test_dir, feature_dir)
            if os.path.isdir(feature_path):
                for filename in os.listdir(feature_path):
                    if filename.endswith(".lox"):
                        _generate_test_case_from_file(
                            test_dir=test_dir,
                            feature_dir=feature_dir,
                            filename=filename,
                            output_file=f
                        )

        f.write("# The end of generated test cases\n")


def _generate_test_case_from_file(test_dir, feature_dir, filename, output_file):
    lox_file = os.path.join("lox", feature_dir, filename).replace(os.path.sep, '/')
    test_name = f"test_{feature_dir}_{filename[:-4]}"
    test_case = f"def {test_name}(lox, line_ending):\n"
    test_case += f"    res = run_lox(lox, '{lox_file}')\n\n"
    expected_values = []
    expected_runtime_errors = []
    expected_compile_errors = []

    # Read the .lox file to extract expected values
    with open(os.path.join(test_dir, feature_dir, filename), 'r', encoding="utf-8") as lox_file:
        for line in lox_file:
            stripped_line = line.strip()
            if stripped_line.find("// expect:") >= 0:
                expected_values.append(stripped_line.split(":")[1].strip())
            elif stripped_line.find("// expect runtime error:") >= 0:
                expected_runtime_errors.append(stripped_line.split(":")[1].strip())
            elif stripped_line.find("[line") >= 0:
                expected_compile_errors.append(stripped_line.split("//")[1].strip())

    if not expected_compile_errors:
        test_case += f"    expected = [\n"

        for value in expected_values:
            test_case += f"        \"{value}\",\n"

        test_case += "    ]\n\n"

        test_case += "    expected_runtime_errors = [\n"
        for value in expected_runtime_errors:
            test_case += f"        \"{value}\",\n"
        test_case += "    ]\n\n"
    else:
        test_case += "    expected_compile_errors = [\n"
        for value in expected_compile_errors:
            test_case += f"        \"\"\"{value}\"\"\",\n"
        test_case += "    ]\n\n"

    exit_code = 0

    if expected_runtime_errors:
        exit_code = 70
    if expected_compile_errors:
        exit_code = 65

    test_case += f"    expected_exit_code = {exit_code}\n"
    test_case += f"    assert res.exit_code == expected_exit_code, {EXIT_CODE_ASSERTION_MESSAGE}\n\n"

    if not expected_compile_errors:
        test_case += "    std_out = res.stdout.replace(line_ending, \"\\n\").splitlines()\n"
        test_case += "    for expected_output, actual_output in zip(expected, std_out):\n"
        test_case += "        assert (\n"
        test_case += "            actual_output.strip() == expected_output.strip(),\n"
        test_case += ASSERTION_MESSAGE
        test_case += "        )\n\n"

        test_case += "    std_err = res.stderr.replace(line_ending, \"\\n\").splitlines()\n"
        test_case += "    for expected_output, actual_output in zip(expected_runtime_errors, std_err):\n"
        test_case += "        assert (\n"
        test_case += "            actual_output.strip() == expected_output.strip(),\n"
        test_case += ASSERTION_MESSAGE
        test_case += "        )\n\n"
    else:
        test_case += "    std_err = res.stderr.replace(line_ending, \"\\n\").splitlines()\n"
        test_case += "    for expected_output, actual_output in zip(expected_compile_errors, std_err):\n"
        test_case += "        assert (\n"
        test_case += "            actual_output.strip() == expected_output.strip(),\n"
        test_case += ASSERTION_MESSAGE
        test_case += "        )\n\n"

    test_case += "\n"
    output_file.write(test_case)


if __name__ == "__main__":
    generate_test_cases("lox", "test_lox_generated.py")
