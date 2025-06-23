package features.snel.chroma;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.snel.SnEL;
import org.noear.solon.expression.transform.chroma.ChromaFilterTransformer;

import java.util.Map;

/**
 * @author noear 2025/6/23 created
 */
public class FilterTest {
    @Test
    public void case1() {
        Expression<Boolean> expr = SnEL.parse("age > 18 AND status=='active'");

        Map tmp = ChromaFilterTransformer.getInstance().transform(expr);

        System.out.println(tmp);

        Assertions.assertEquals("{$and=[{age={$gt=18}}, {status=active}]}", tmp.toString());
    }
}
