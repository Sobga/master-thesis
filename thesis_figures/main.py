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
    g._legend.remove()
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

    g = sns.relplot(data=time_benchmark['df'], x='SIZES', y='VALS', hue='COLS', kind='line', style='COLS',
                    facet_kws={'legend_out': True})
    g._legend.set_title("Datastructures")
    # g._legend.remove()

    title = "Accumulated time for random sequence" if fields['RANDOM_OPERATION'] else "Accumulated time for growth operations"
    g.set(xlabel="Number of operations", ylabel="Time [ms]", title=title)
    return g


def plot_memory(mem_benchmark):
    fig, ax = plt.subplots()
    fields = mem_benchmark['FIELDS']
    # mem_benchmark['DATA'].pop('ArrayList')
    mem_benchmark['df'] = pd.DataFrame(mem_benchmark['DATA'])

    sns.lineplot(data=mem_benchmark['df'], ax=ax)
    sns.lineplot(data=fields['ACTUAL_SIZE'], ax=ax, color='black', label='Actual size')

    title = "Memory consumption"
    ax.set(xlabel="Operation index", ylabel="Memory [Words]", title=title)
    sns.despine()
    return fig


def plot_warmup(benchmark):
    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    fields = benchmark['FIELDS']
    df = pd.DataFrame({**benchmark['DATA'], **{'SIZES': fields['INTERVALS']}})
    benchmark['df'] = df.melt('SIZES', var_name='COLS', value_name='VALS')

    g = sns.relplot(data=benchmark['df'], x='SIZES', y='VALS', hue='COLS', kind="line", palette="magma", markers=True)
    g._legend.remove()

    title = "Warmup"
    g.set(xlabel="Number of operations", ylabel="Time [ms]", title=title)
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
    elif 'Warmup' in benchmark['NAME']:
        return plot_warmup(benchmark)
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
    plt.show()


if __name__ == '__main__':
    main()
