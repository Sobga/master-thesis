import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt
from matplotlib.ticker import FormatStrFormatter, ScalarFormatter

from pgfplots import simplify_mem_data
from plotstyles import lookup_style, mark_freq, display_name


def plot_times(time_benchmark):
    fig, ax = plt.subplots()
    fig.set_size_inches(w=4.7, h=4)
    fields = time_benchmark['FIELDS']

    for name, data in time_benchmark['DATA'].items():
        color, marker = lookup_style(name)
        indices = [i for i, x in enumerate(data) if x > 100 and i < 20000]
        ax.plot(indices, [data[i] for i in indices], 'o', marker=marker, color=color, label=display_name(name))
    title = "Time pr. operation" if fields['RANDOM_OPERATION'] else "Time pr. growth operation"
    ax.set(xlabel="Number of items stored", ylabel="Time [ns]", title=title)
    ax.legend()
    return fig


def plot_total_time(time_benchmark):
    fig, ax = plt.subplots()
    # Rescale measurements from ns to ms
    for arr in time_benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6
    fields = time_benchmark['FIELDS']
    intervals = fields['INTERVALS']

    for name, data in time_benchmark['DATA'].items():
        color, marker = lookup_style(name)
        ax.plot(intervals, data, color=color, marker=marker, markevery=mark_freq(name, len(data)), label=name)
    ax.legend()
    title = "Accumulated time for growth operations"
    ax.set(xlabel="No. of growth operations completed", ylabel="Time [ms]", title=title)
    return fig


def plot_boxplot_time(benchmark):
    fig, ax = plt.subplots()

    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    df = pd.DataFrame(benchmark['DATA'])
    palette = {key: lookup_style(key)[0] for key in benchmark['DATA'].keys()}
    palette['BrodnikPowerTwo'] = 'white'
    sns.boxplot(df, ax=ax, palette=palette)
    fig.autofmt_xdate(rotation=20)
    ax.set(xlabel="Resizable array", ylabel="Time [ms]", title="Time to complete $10^7$ growth operations")
    return fig


def plot_indexing(benchmark):
    fig, ax = plt.subplots()

    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    fields = benchmark['FIELDS']
    intervals = fields['INTERVALS']
    for name, data in benchmark['DATA'].items():
        color, marker = lookup_style(name)
        ax.plot(intervals, data, color=color, marker=marker, markevery=mark_freq(name, len(data)), label=name)
    ax.legend()
    title = "Accumulated time for indexing operations"
    ax.set(xlabel="No. of indexing operations completed", ylabel="Time [ms]", title=title)
    return fig


def plot_increasing_indexing(benchmark):
    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    fields = benchmark['FIELDS']
    df = pd.DataFrame({**benchmark['DATA'], **{'SIZES': fields['SIZES']}})
    benchmark['df'] = df.melt('SIZES', var_name='COLS', value_name='VALS')

    g = sns.relplot(data=benchmark['df'], x='SIZES', y='VALS', hue='COLS', kind='line', style='COLS',
                    facet_kws={'legend_out': True})
    g._legend.set_title("Datastructures")
    # g._legend.remove()

    title = "Accumulated time for indexing operations"
    g.set(xlabel="Size of array and permutation", ylabel="Time [ms]", title=title)
    return g


def plot_boxplot_indexing(benchmark):
    fig, ax = plt.subplots()

    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    df = pd.DataFrame(benchmark['DATA'])
    palette = {key: lookup_style(key)[0] for key in benchmark['DATA'].keys()}
    palette['BrodnikPowerTwo'] = 'white'
    sns.boxplot(df, ax=ax, palette=palette, showfliers=False)
    fig.autofmt_xdate(rotation=20)
    ax.set(xlabel="Resizable array", ylabel="Time [ms]", title="Time to complete $10^7$ indexing operations")
    return fig


def plot_boxplot_shrinking(benchmark):
    fig, ax = plt.subplots()

    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    df = pd.DataFrame(benchmark['DATA'])
    palette = {key: lookup_style(key)[0] for key in benchmark['DATA'].keys()}
    palette['BrodnikPowerTwo'] = 'white'
    sns.boxplot(df, ax=ax, palette=palette, showfliers=False)
    fig.autofmt_xdate(rotation=20)
    ax.set(xlabel="Resizable array", ylabel="Time [ms]", title="Time to complete $10^7$ shrinking operations")
    return fig


def plot_memory(mem_benchmark):
    fig_a = memory_plots(mem_benchmark, "Static overhead")

    mem_benchmark['DATA'].pop('ArrayList', None)
    mem_benchmark['DATA'].pop('ConstantArray-1.0', None)
    mem_benchmark['DATA'].pop('ConstantLazyArray-1.0', None)

    fig_b = memory_plots(mem_benchmark, "Static overhead")
    return [fig_a, fig_b]


def memory_plots(mem_benchmark, title):
    fig, ax = plt.subplots()
    fig.set_size_inches(w=2.4, h=2.7)

    # ax.yaxis.set_major_formatter(ScalarFormatter('%.1f'))
    # ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0))
    ax.ticklabel_format(style='sci', axis='y', scilimits=(0, 0), useMathText=True)
    fields = mem_benchmark['FIELDS']

    if fields['N_SHRINK'] == 0:
        title += " - Grow operations"
        for name, data in mem_benchmark['DATA'].items():
            color, style = lookup_style(name)
            ax.plot(data[0], data[1], color=color, linewidth=0.5, label=display_name(name))
    else:
        title += " - Shrink operations"
        n_measurements = len(fields['ACTUAL_SIZE'])

        for name, data in mem_benchmark['DATA'].items():
            color, style = lookup_style(name)
            ax.plot(n_measurements - data[0], data[1], color=color, linewidth=0.5, label=display_name(name))
        ax.invert_xaxis()
    ax.set(xlabel="Number of items stored", ylabel="Memory [Words]", title=title)

    # ax.legend(loc='upper center', bbox_to_anchor=(0.5, -0.05), ncol=5)
    ax.legend()
    return fig


def plot_rebuild(mem_benchmark):
    mem_benchmark['DATA'].pop('ArrayList')

    fig_a = memory_plots(mem_benchmark, "Dynamic overhead")
    mem_benchmark['DATA'].pop('ConstantArray-1.0')
    mem_benchmark['DATA'].pop('ConstantLazyArray-1.0')
    fig_b = memory_plots(mem_benchmark, "Dynamic overhead")

    return [fig_a, fig_b]


def rebuild_plots(mem_benchmark):
    fig, ax = plt.subplots()
    fields = mem_benchmark['FIELDS']

    for name, data in mem_benchmark['DATA'].items():
        color, style = lookup_style(name)
        ax.plot(data, color=color, markevery=mark_freq(name, len(data)), label=name)

    title = "Overhead of datastructures, incl. rebuilding"
    if fields['N_SHRINK'] == 0:
        title += " - Grow operations"
    else:
        title += " - Shrink operations"

    ax.set(xlabel="No. of growth operations", ylabel="Memory [Words]", title=title)
    ax.legend()
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
    name = benchmark['NAME']

    if 'Memory' in name:
        simplify_mem_data(benchmark)
    if 'TotalTime' in name:
        return plot_total_time(benchmark)
        pass
    elif 'TimeBoxPlot' in name:
        return plot_boxplot_time(benchmark)
        pass
    elif 'Time' in name:
        return plot_times(benchmark)
        pass
    elif 'Rebuild' in name:
        return plot_rebuild(benchmark)
        pass
    elif 'Memory' in name:
        return plot_memory(benchmark)
        pass
    elif 'Warmup' in name:
        return plot_warmup(benchmark)
        pass
    elif 'IncreasingIndexing' in name:
        return plot_increasing_indexing(benchmark)
        pass
    elif 'IndexingBoxPlot' in name:
        return plot_boxplot_indexing(benchmark)
    elif 'Indexing' in name:
        return plot_indexing(benchmark)
        pass
    elif 'Shrink' in name:
        return plot_boxplot_shrinking(benchmark)
        pass
    else:
        raise Exception("No plot found for " + benchmark['NAME'])
