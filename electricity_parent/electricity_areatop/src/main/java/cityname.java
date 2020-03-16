import com.electricity.areatop.app.AreaTop;
import com.electricity.areatop.model.OrderCountInfo$;
import org.apache.spark.SparkConf;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.math.BigInt;

public class cityname {
    public static void main(String[] args) {
        AreaTop.main(new String[]{});

        SparkConf sparkConf = new SparkConf().setAppName("session").setMaster("local[*]");
        SparkSession sparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate();


        RDD<Tuple2<String, BigInt>> areaCountRDD = AreaTop.getAreaCountRDD(sparkSession);
        System.out.println(areaCountRDD.toString());
    }

}
