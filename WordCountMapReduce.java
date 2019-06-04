import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.StringTokenizer;

public class WordCountMapReduce extends Configured implements Tool {

    // Mapper Class
    public static class MyMapper extends
            Mapper<LongWritable, Text, Text, IntWritable> {
        /*
         * key是偏移量，value是一行一行的值 首先分割单词，组成key/value对进行输出
         */
        private Text mapOutputKey = new Text();
        private final static IntWritable mapOutputValue = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // todo
            String line = value.toString().trim();

            //segment
            StringTokenizer strToken = new StringTokenizer(line);

            while(strToken.hasMoreTokens()){
                String word = strToken.nextToken();
                mapOutputKey.set(word);
                context.write(mapOutputKey, mapOutputValue);
            }


        }
    }

    // Reducer
    public static class MyReducer extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable reduceOutputValue = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values,
                              Context context) throws IOException, InterruptedException {
            // todo

            int sum = 0;
            //reduce
            for(IntWritable value : values){
                sum+=value.get();
            }
            reduceOutputValue.set(sum);
            context.write(key, reduceOutputValue);
        }

    }

    public int run(String[] args) throws Exception {
        // set Conf env
        Configuration conf = new Configuration();
        // conf.set("mapreduce.map.output.compress", true);

        // get job by conf
        Job job = Job.getInstance(super.getConf(),
                WordCountMapReduce.class.getSimpleName());

        job.setJarByClass(WordCountMapReduce.class);

        // set job
        // step 1 : map phase
        job.setMapperClass(MyMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // step 2 :reduce phase
        job.setCombinerClass(MyReducer.class);
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // submit
        // job.submit();
        boolean isSucceed = job.waitForCompletion(true);

        return isSucceed ? 1 : 0;

    }

    // Driver
    public static void main(String[] args) throws Exception {

        args = new String[] { "hdfs://192.168.55.225:8022/user/agp/hdfs-site.xml",
                "hdfs://192.168.55.225:8022/user/agp/out/01" };

        int status = ToolRunner.run(new WordCountMapReduce(), args);

        System.out.println(status);

    }

}
