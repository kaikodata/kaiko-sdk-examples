import subprocess
import os

function_names = [
    "trades_request",
    "ohlcv_request",
    "vwap_request",
    "index_rate_request",
    "index_multi_asset",
    "index_forex_rate",
    "aggregated_quote_request",
    "market_update_request",
    "aggregates_spot_exchange_rate_request",
    "aggregates_direct_exchange_rate_request",
    "derivatives_instrument_metrics_request",
    "iv_svi_parameters_v1_request",
    "exotic_indices_v1_request",
    "aggregated_state_price_v1_request",
]

only_script_functions = {
    "derivatives_instrument_metrics_request",
    "aggregated_quote_request",
    "aggregates_spot_exchange_rate_request",
    "aggregates_direct_exchange_rate_request",
    "index_forex_rate",
    "iv_svi_parameters_v1_request",
}

script_path = "./python_generator.py"
replay_script_path = "./python_replay_generator.py"

def generate_code():
    for function_name in function_names:
        try:
            subprocess.run(["python", script_path, function_name], check=True)
            print(f"Generated code for {function_name} using script.py")

            if function_name not in only_script_functions:
                subprocess.run(["python", replay_script_path, function_name], check=True)
                print(f"Generated replay code for {function_name} using replay-script.py")

        except subprocess.CalledProcessError as e:
            print(f"Error generating code for {function_name}: {e}")
            exit(1)

if __name__ == "__main__":
    output_dir = "../code-example"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    generate_code()