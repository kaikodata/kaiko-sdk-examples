import sys
from pathlib import Path
from utils import get_relevant_code

if __name__ == "__main__":
    file_path = "../endpoints.py"
    function_name = sys.argv[1]
    output_path = f"../code-example/{function_name}.py"
    output_dir = Path(output_path).parent

    extracted_code = get_relevant_code(file_path, function_name)
    Path(output_path).write_text(extracted_code)
    print(f"Extracted code saved to {output_path}")