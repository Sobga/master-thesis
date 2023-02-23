import json
import os
import seaborn as sns
import pandas as pd
import matplotlib.pyplot as plt

BENCHMARK_DIR = "../benchmarks"


def plot_times(time_benchmark):
    time_benchmark['df'] = pd.DataFrame(time_benchmark['DATA'])
    ax = sns.relplot(data=time_benchmark['df'])
    ax.set(xlabel="Operation index", ylabel="Time [ns]", title="Time pr. growth operation")


def plot_benchmark(benchmark):
    if 'TimeBenchmark' in benchmark['NAME']:
        plot_times(benchmark)
    else:
        raise Exception("No plot found for " + benchmark['NAME'])


def main():
    sns.set_theme()
    filenames = sorted(os.listdir(BENCHMARK_DIR), reverse=True)
    benchmarks = []

    with open(os.path.join(BENCHMARK_DIR, filenames[0]), "r") as file:
        benchmarks = json.load(file)
        file.close()

    for benchmark in benchmarks:
        plot_benchmark(benchmark)
    plt.show()


if __name__ == '__main__':
    main()
