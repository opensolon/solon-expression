package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.Utils;
import org.noear.solon.core.util.Assert;
import org.noear.solon.expression.snel.SnEL;

import java.util.Map;

/**
 *
 * @author noear 2025/9/29 created
 *
 */
public class PropertiesTest {
    @Test
    public void case1() {
        assert testExpression("${yyy.enable}", Utils.asMap("yyy.enable", "true"));
        assert testExpression("${yyy.enable}", Utils.asMap()) == false;
        assert testExpression("${yyy.enable} == false", Utils.asMap());
        assert testExpression("${yyy.enable} == 'true'", Utils.asMap("yyy.enable", "true"));
    }

    @Test
    public void case1_2() {
        assert testExpression("${yyy.enable}", Utils.asMap()) == false;
        assert testExpression("${yyy.enable} == 'true'", Utils.asMap()) == false;
    }

    @Test
    public void case2() {
        assert testExpression("${yyy.enable:true} == 'true'", Utils.asMap());
        assert testExpression("${yyy.enable:true} != 'true'", Utils.asMap()) == false;
    }

    @Test
    public void case3() {
        assert testExpression("${yyy.enable} && ${zzz.enable:true} == 'false'", Utils.asMap()) == false;
        assert testExpression("${yyy.enable} && ${zzz.enable:true} == 'false'", Utils.asMap("yyy.enable", "1")) == false;
        assert testExpression("${yyy.enable} && ${zzz.enable:true} == 'false'", Utils.asMap("yyy.enable", "1", "zzz.enable", "false")) == true;
    }

    @Test
    public void case4() {
        assert testExpression("${yyy.enable:true} == true", Utils.asMap());
        assert testExpression("${yyy.enable:true} != true", Utils.asMap()) == false;
    }

    @Test
    public void case5() {
        assert testExpression("${yyy:1} > 0", Utils.asMap());
        assert testExpression("${yyy:2} < 1", Utils.asMap()) == false;
        assert testExpression("${yyy:2} < 1", Utils.asMap("yyy", "0"));
    }


    private static boolean testExpression(String expr, Map context) {
        Object val = SnEL.eval(expr, context);

        if (val instanceof Boolean) {
            return (Boolean) val;
        }

        if (val instanceof String) {
            //如果是字符串，有值就行
            return Assert.isNotEmpty((String) val);
        }

        //其它，非 null 就行
        return val != null;
    }
}
