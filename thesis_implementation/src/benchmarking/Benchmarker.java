package benchmarking;

import resizableArrays.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class Benchmarker {

    private void appendString(StringBuilder sb, String str){
        sb.append("\"");
        sb.append(str);
        sb.append("\"");
    }

    private void appendProperty(StringBuilder sb, String name, Object property){
        appendString(sb, name);
        sb.append(": ");
        if (property instanceof String)
            appendString(sb, (String) property);
        else if (property instanceof long[])
            sb.append(Arrays.toString((long[]) property));
        else if (property instanceof  int[])
            sb.append(Arrays.toString((int[]) property));
        else
            sb.append(property);
        sb.append("\n");
    }

    private String benchmarkToJSON(Benchmark benchmark){
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("{");
        appendProperty(sb, "NAME", benchmark.getName());


        // Meta-Fields
        sb.append(", \"FIELDS\": {");
        Map<String, Object> fields = benchmark.getJSONFields();
        int i = 0;
        for (Map.Entry<String, Object> entry: fields.entrySet()){
            if (i++ != 0)
                sb.append(", ");
            appendProperty(sb, entry.getKey(), entry.getValue());
        }
        // End of fields
        sb.append("}");

        // Data
        sb.append(", \"DATA\": {");
        i = 0;
        for (ResizableArray<Integer> array: benchmark.getArrays()){
            Object results = benchmark.getRecordedData(array);
            if (i++ != 0)
                sb.append(", ");
            appendProperty(sb, benchmark.getArrayName(array), results);
        }
        // End of data
        sb.append("}");

        // End of object
        sb.append("}");
        return sb.toString();
    }

    private String toJSON(Benchmark[] benchmarks){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < benchmarks.length; i++){
            if (i != 0)
                sb.append(", ");
            sb.append(benchmarkToJSON(benchmarks[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    public void run(){
        ResizableArray<Integer>[] arrays = new ResizableArray[]
                {
                        new ResArrayList<Integer>(),
                        new ConstantArray<Integer>(1),
                        new ConstantLazyArray<Integer>(1),
                        new Brodnik<Integer>(),
                        new BrodnikPowerTwo<Integer>(),
                        new Sitarski<Integer>(),
                        new Tarjan<Integer>(),
                        new VBArray<Integer>()
                };

        // Selected benchmarks
        Benchmark[] benchmarks = new Benchmark[]{
//            new TimeBenchmark(arrays, false),
//            new TimeBoxPlotBenchmark(arrays, false),
//            new IndexingBoxPlotBenchmark(arrays),
            new LinearBoxPlotBenchmark(arrays),
//            new ShrinkBenchmark(arrays),
//            new RebuildMemoryBenchmark(arrays),
//            new RebuildMemoryBenchmark(arrays, (int) 1E6, 0, (int) 1E6),
//            new MemoryBenchmark(arrays, 0, (int) 1E6, 0),
//            new MemoryBenchmark(arrays, (int) 1E6, 0, (int) 1E6),
        };

        // Perform benchmarks
        for (Benchmark benchmark: benchmarks){
            System.out.println("Running benchmark: " + benchmark.getName());
            benchmark.run();

            // Clean for next benchmark
            for (ResizableArray<Integer> array: arrays)
                array.clear();
        }


        // Write results
        try {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("(yyyy-MM-dd)(HH-mm-ss)");
            PrintWriter writer = new PrintWriter("benchmarks/bench" + formatter.format(now) +".txt", StandardCharsets.UTF_8);
            writer.write(toJSON(benchmarks));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Benchmarker().run();
    }
}
