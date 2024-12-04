import sys
from pathlib import Path
from utils import export_code_examples, get_relevant_code

if __name__ == "__main__":
    file_path = "../endpoints.py"
    function_name = sys.argv[1]
    output_path = f"../code-example/{function_name}_replay.py"

    extracted_code = get_relevant_code(file_path, function_name, modify_function=True)
    export_code_examples(function_name, output_path, extracted_code)