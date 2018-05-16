import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WordCount {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      Integer docId = Integer.parseInt(itr.nextToken());
      IntWritable docIdWritable = new IntWritable(docId);
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, docIdWritable);
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text,IntWritable,Text,Text> {
    //MapWritable result = new MapWritable();

      public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
      Map<Integer, Integer> res = new HashMap<>();
      for (IntWritable val : values) {
        Integer key1 = new Integer(val.get());
        if(res.containsKey(key1)) {
          Integer tmp = res.get(key1) + 1;
          res.put(key1, tmp);
        } else {
          res.put(key1, new Integer(1));
        }
      }
      String tmp = "";
      for(Entry<Integer,Integer> entry : res.entrySet()){
         tmp = entry.getKey() + ":" + entry.getValue() + " "  + tmp;
      }
      context.write(key, new Text(tmp));
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);    
    job.setOutputKeyClass(Text.class); 
    job.setOutputValueClass(Text.class);
    
    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
