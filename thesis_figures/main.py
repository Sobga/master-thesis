import json
import os
import seaborn as sns
import pandas as pd
import matplotlib.pyplot as plt

BENCHMARK_DIR = "../benchmarks"


def plot_times(time_benchmark):
    time_benchmark['df'] = pd.DataFrame(time_benchmark['DATA'])
    fields = time_benchmark['FIELDS']
    g = sns.relplot(data=time_benchmark['df'], facet_kws={'legend_out': True})
    g._legend.set_title("Datastructures")
    title = "Time pr. operation" if fields['RANDOM_OPERATION'] else "Time pr. growth operation"
    g.set(xlabel="Operation index", ylabel="Time [ns]", title=title)
    return g


def plot_total_time(time_benchmark):
    # Rescale measurements from ns to ms
    for arr in time_benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    fields = time_benchmark['FIELDS']
    df = pd.DataFrame({**time_benchmark['DATA'], **{'SIZES': fields['INTERVALS']}})
    time_benchmark['df'] = df.melt('SIZES', var_name='COLS', value_name='VALS')

    g = sns.relplot(data=time_benchmark['df'], x='SIZES', y='VALS', hue='COLS', kind='line', style='COLS', markers=True, facet_kws={'legend_out': True})
    g._legend.set_title("Datastructures")

    title = "Overall time for random sequence" if fields['RANDOM_OPERATION'] else "Overall time for growth operations"
    g.set(xlabel="Number of operations", ylabel="Time [ms]", title=title)
    return g


def plot_memory(mem_benchmark):
    fields = mem_benchmark['FIELDS']
    mem_benchmark['df'] = pd.DataFrame({**mem_benchmark['DATA'], **{'ACTUAL_SIZE': fields['ACTUAL_SIZE']}})

    g = sns.relplot(data=mem_benchmark['df'], facet_kws={'legend_out': True})
    g._legend.set_title("Datastructures")

    title = "Memory consumption"
    g.set(xlabel="Operation index", ylabel="Memory [Words]", title=title)

    return g


def plot_benchmark(benchmark):
    if 'TotalTime' in benchmark['NAME']:
        return plot_total_time(benchmark)
        pass
    elif 'Time' in benchmark['NAME']:
        return plot_times(benchmark)
        pass
    elif 'Memory' in benchmark['NAME']:
        return plot_memory(benchmark)
        pass
    else:
        raise Exception("No plot found for " + benchmark['NAME'])


def main():
    # sns.set_theme()
    filenames = sorted(os.listdir(BENCHMARK_DIR), reverse=True)

    with open(os.path.join(BENCHMARK_DIR, filenames[0]), "r") as file:
        benchmarks = json.load(file)
        file.close()

    for idx, benchmark in enumerate(benchmarks):
        g = plot_benchmark(benchmark)
        name = benchmark['NAME']
        g.savefig(f'Figures/{name}_{idx}.png', format='png')
    # plt.show()


if __name__ == '__main__':
    main()
