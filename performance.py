#!/usr/bin/env python3

import os
import sys
import threading
import json
import multiprocessing.dummy as mp
from itertools import product
from shutil import copyfile

SIMUTALOR = 'java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator'
TEMPLATE_2PC = 'configs/config-2pc-template.txt'
TEMPLATE_TSB = 'configs/config-tsb-template.txt'
PERFOMANCE_FOLDER = 'performance_files'
PERFOMANCE_RESULT = f'{PERFOMANCE_FOLDER}/performance.json'

performance_data = []
tests_to_do = 0
tests_done = 0

def simulate(args):
    global tests_done
    tests_done += 1

    thread_name = threading.current_thread().name
    print(f'{thread_name} : Tests done: {tests_done}/{tests_to_do}')
    bandwidth, cpu_delay, distance_delay = args

    config_2pc_file = f'{PERFOMANCE_FOLDER}/{thread_name}-2pc.config'
    config_tsb_file = f'{PERFOMANCE_FOLDER}/{thread_name}-tsb.config'
    data = {"bandwidht": bandwidth, "cpu_delay" : cpu_delay, "distance_delay" : distance_delay}

    os.system(f"sed -e 's/BANDWIDTH/{bandwidth}/' -e 's/CPU_DELAY/{cpu_delay}/' -e 's/DELAY_PER_DISTANCE/{distance_delay}/' < {TEMPLATE_2PC} > {config_2pc_file}")
    result = os.popen(f'{SIMUTALOR} {config_2pc_file} 2>/dev/null').read().strip('\n')
    data["2pc"] = int(result)
    os.remove(config_2pc_file)

    tsb_data = []
    for batch_size in range(0, 60, 10):
        if batch_size == 0:
            batch_size = 1

        os.system(f"sed -e 's/BANDWIDTH/{bandwidth}/' -e 's/CPU_DELAY/{cpu_delay}/' -e 's/DELAY_PER_DISTANCE/{distance_delay}/' -e 's/BATCH_SIZE/{batch_size}/' < {TEMPLATE_TSB} > {config_tsb_file}")
        result = os.popen(f'{SIMUTALOR} {config_tsb_file} 2>/dev/null').read().strip('\n')
        tsb_data.append((batch_size, int(result)))
        os.remove(config_tsb_file)

    data["tsb"] = tsb_data
    performance_data.append(data)

    with open(PERFOMANCE_RESULT, 'w') as file:
        file.write(json.dumps(performance_data))
        file.close()

def check_if_saved(bandwidth, cpu_delay, distance_delay, data):
    # checks if tests was already saved
    if len(list(filter(lambda e: e["bandwidht"] == bandwidth and e["cpu_delay"] == cpu_delay and e["distance_delay"] == distance_delay, data))) != 0:
        print(f'Test (bandwidth={bandwidth}, cpu_delay={cpu_delay}, distance_delay={distance_delay}) was already done', flush=True)
        return True


if __name__ == "__main__":

    # loads saved results
    with open(PERFOMANCE_RESULT, 'r') as file:
        performance_data = json.load(file)
        file.close()

    # bandwidth = [250_000, 2_500_000], cpu_delay = [0, 500], distance_delay = [16, 8192]
    args_to_test = list(product(range(250_000, 2_750_000, 250_000), range(0, 550, 50), [16 * (2 ** i) for i in range(10)]))
    print(f'Total number of tests: {len(args_to_test)}')
    # filters out the dones already done
    args_to_test = list(filter(lambda e: not check_if_saved(e[0], e[1], e[2], performance_data), args_to_test))
    tests_to_do = len(args_to_test)
    print(f'Number of tests to do: {tests_to_do}')

    number_threads = 4

    try:
        number_threads = int(sys.argv[1])
    except:
        pass

    print(f'Running tests on {number_threads} threads')
    pool = mp.Pool(number_threads)
    pool.map(simulate, args_to_test)
    pool.close()
    pool.join()
