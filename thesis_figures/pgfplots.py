import numpy as np

from plotstyles import lookup_style
from scipy.signal import argrelextrema
import sortednp

MS_TO_NS = 1E6


def pgf_boxplot_indexing(benchmark):
    # Rescale measurements from ns to ms
    for arr in benchmark['DATA'].values():
        for i in range(len(arr)):
            arr[i] /= 1E6

    for key, data in benchmark['DATA'].items():
        box_value = compute_boxvalue(data)
        color, _ = lookup_style(key)
        if color == 'black':
            color = 'white'
        print(f'\\addplot[fill={color},boxplot prepared={{{format_boxvalue(box_value)}}}] coordinates {{}};')
    # df = pd.DataFrame(benchmark['DATA'])
    # palette = {key: lookup_style(key)[0] for key in benchmark['DATA'].keys()}
    # palette['BrodnikPowerTwo'] = 'white'
    # sns.boxplot(df, ax=ax, palette=palette, showfliers=False)
    # fig.autofmt_xdate(rotation=20)
    # ax.set(xlabel="Resizable array", ylabel="Time [ms]", title="Time to complete $10^7$ indexing operations")
    # return fig


def format_boxvalue(data):
    output = ''
    for idx, kv in enumerate(data.items()):
        key, value = kv

        if idx != 0:
            output += ', '
        output += f'{key}={value}'
    return output


def compute_boxvalue(data):
    Q1, median, Q3 = np.percentile(np.asarray(data), [25, 50, 75])
    IQR = Q3 - Q1

    loval = Q1 - 1.5 * IQR
    hival = Q3 + 1.5 * IQR

    wiskhi = np.compress(data <= hival, data)
    wisklo = np.compress(data >= loval, data)
    actual_hival = np.max(wiskhi)
    actual_loval = np.min(wisklo)

    return {
        'median': median,
        'upper quartile': Q1,
        'lower quartile': Q3,
        'lower whisker': actual_loval,
        'upper whisker': actual_hival
    }


def simplify_mem_data(benchmark):
    fields = benchmark['FIELDS']
    sizes = fields['ACTUAL_SIZE'] = np.array(fields['ACTUAL_SIZE'])

    for r_name, arr in benchmark['DATA'].items():
        overhead = np.array(arr) - sizes
        #indices = sortednp.merge(argrelextrema(overhead, np.greater_equal)[0], argrelextrema(overhead, np.less_equal)[0])
        #indices = np.append(0, indices)
        #indices = np.append(indices, len(arr) - 1)
        indices = np.arange(len(overhead)-1)
        values = overhead[indices]
        benchmark['DATA'][r_name] = indices, values

