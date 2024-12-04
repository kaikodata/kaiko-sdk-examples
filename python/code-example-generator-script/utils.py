import ast
from pathlib import Path
from kaikosdk.stream.market_update_v1 import commodity_pb2 # This is used by descriptor_pool
from google.protobuf.descriptor_pool import Default


def get_relevant_code(file_path, function_name, modify_function=False):
    """
    Extract relevant imports, function definition, and optionally modify the `run` function and main block.
    """
    source_code = Path(file_path).read_text()

    # Parse the file into an AST
    tree = ast.parse(source_code)

    # Collect all imports
    imports = [node for node in tree.body if isinstance(node, (ast.Import, ast.ImportFrom))]

    # Locate the target function definition
    function_def = next(
        (node for node in tree.body if isinstance(node, ast.FunctionDef) and node.name == function_name), None
    )
    if not function_def:
        raise ValueError(f"Function {function_name} not found in {file_path}.")

    # Extract or modify function code
    function_code = (
        modify_function_code(extract_function_with_comments(source_code, function_def))
        if modify_function
        else extract_function_with_comments(source_code, function_def)
    )

    # Extract relevant imports
    function_imports = {node.id for node in ast.walk(function_def) if isinstance(node, ast.Name)}
    relevant_imports = extract_relevant_imports(imports, function_imports)

    # Modify `run` function if present
    run_code = extract_run_function(tree, function_name)

    # Extract the main block
    main_block = extract_main_block(tree)

    # Combine all components
    comment_text = "# This is a code example. Configure your parameters below #"
    return f"{comment_text}\n\n{relevant_imports}\n\n{function_code}\n\n{run_code}\n\n{main_block}".strip()


def extract_relevant_imports(imports, function_imports):
    """
    Filter and return relevant imports as code.
    """
    predefined_imports = [
        "from __future__ import print_function",
        "from datetime import datetime, timedelta",
        "import logging",
        "import os",
        "from google.protobuf.timestamp_pb2 import Timestamp",
    ]
    relevant_imports = [ast.parse(imp).body[0] for imp in predefined_imports]
    for node in imports:
        if isinstance(node, ast.Import):
            for alias in node.names:
                base_name = alias.asname or alias.name.split('.')[0]
                if base_name in function_imports:
                    relevant_imports.append(node)
                    break
        elif isinstance(node, ast.ImportFrom) and node.module:
            used_aliases = [
                alias for alias in node.names 
                if alias.name in function_imports or (alias.asname and alias.asname in function_imports)
            ]
            if used_aliases:
                relevant_imports.append(
                    ast.ImportFrom(module=node.module, names=used_aliases, level=node.level)
                )
    return "\n".join(ast.unparse(imp) for imp in relevant_imports)


def modify_function_code(original_code):
    """
    Modify the function code to include date configuration and interval handling.
    """
    date_config = """# start of date configuration #
        start = Timestamp()
        start.FromDatetime(datetime.utcnow() - timedelta(days=2))
        end = Timestamp()
        end.FromDatetime(datetime.utcnow() - timedelta(days=1))
        # end of date configuration #"""
    modified_code = original_code.replace("with channel:", "").replace(
        "try:", f"try:\n        {date_config}"
    ).replace(
        "\n            ))\n            for response in responses:",
        ",\n                interval={\n                    'start_time': start,\n                    'end_time': end\n                }\n            ))\n            for response in responses:",
    )
    return adjust_stub_indentation(modified_code)


def adjust_stub_indentation(code):
    """
    Adjust indentation for lines within the stub block.
    """
    lines = code.splitlines()
    adjusted_lines = []
    inside_block = False

    for line in lines:
        stripped = line.lstrip()
        if 'stub' in stripped or 'for response' in stripped:
            inside_block = True
            adjusted_lines.append(line[4:])
        elif inside_block and (stripped.startswith("),") or stripped.endswith(")))")):
            adjusted_lines.append(line[4:])
            inside_block = False
        elif inside_block:
            adjusted_lines.append(line[4:])
        else:
            adjusted_lines.append(line)

    return "\n".join(adjusted_lines)


def extract_function_with_comments(source_code, function_def):
    """
    Extract function's code from source, including comments.
    """
    lines = source_code.splitlines()
    return "\n".join(lines[function_def.lineno - 1:function_def.end_lineno])


def extract_run_function(tree, function_name):
    """
    Extract and modify the `run` function, replacing placeholder calls with the target function name.
    """
    for node in tree.body:
        if isinstance(node, ast.FunctionDef) and node.name == "run":
            return ast.unparse(node).replace("trades_request(channel)", f"{function_name}(channel)")
    return ""


def extract_main_block(tree):
    """
    Extract the `if __name__ == '__main__'` block.
    """
    for node in tree.body:
        if isinstance(node, ast.If) and getattr(node.test.left, "id", "") == "__name__":
            return ast.unparse(node)
    return ""


def export_code_examples(function_name, output_path, extracted_code):
    """
    Export the extracted code to a file.
    """
    if function_name == "market_update_request":
        descriptor_pool = Default()
        
        enum_type = descriptor_pool.FindEnumTypeByName("kaikosdk.StreamMarketUpdateCommodity")
        commodity_names = [enum_value.name for enum_value in enum_type.values]
        
        for name in commodity_names:
            Path(output_path[:-3] + '_' + name[5:].lower() + '.py').write_text(extracted_code.replace("SMUC_TRADE", f"{name}"))
            print(f"Extracted code saved to {output_path[:-3] + '_' + name.lower() + '.py'}")
    else:
        Path(output_path).write_text(extracted_code)
        print(f"Extracted code saved to {output_path}")