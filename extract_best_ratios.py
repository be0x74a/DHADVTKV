#!/usr/bin/env python3

import json
from functools import reduce

PERFOMANCE_FOLDER = 'performance_files'
DATA_FILE = f'{PERFOMANCE_FOLDER}/performance.json'
RATIOS_FILE = f'{PERFOMANCE_FOLDER}/ratios.json'

with open(DATA_FILE, 'r') as file:
    data = json.load(file)
    file.close()

ratios = []

for experiment in data:
    best_tsb = max(experiment["tsb"], key=lambda v: v[1])
    ratio = (best_tsb[1] - experiment["2pc"])/experiment["2pc"] if experiment["2pc"] != 0 else -1
    experiment_summary = {
        "bandwidth": experiment["bandwidht"],
        "cpu_delay": experiment["cpu_delay"],
        "distance_delay": experiment["distance_delay"],
        "ratio": ratio,
        "batch_size": best_tsb[0]
    }
    ratios.append(experiment_summary)

ratios.sort(key=lambda e: e["ratio"], reverse=True)

with open(RATIOS_FILE, 'w') as file:
    file.write(json.dumps(ratios, indent=2))
    file.close()