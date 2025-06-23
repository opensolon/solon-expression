package features.snel.es;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.snel.SnEL;
import org.noear.solon.expression.transform.elasticsearch.ElasticsearchFilterTransformer;

import java.util.Map;

/**
 * @author noear 2025/6/23 created
 */
public class FilterTest {
    @Test
    public void case1() {
        Expression<Boolean> expr = SnEL.parse("age > 18 AND status=='active'");

        Map tmp = ElasticsearchFilterTransformer.getInstance().transform(expr);

        System.out.println(tmp);

        Assertions.assertEquals("{bool={must=[{range={age={gt=18}}}, {term={status={value=active}}}]}}", tmp.toString());
    }
}
