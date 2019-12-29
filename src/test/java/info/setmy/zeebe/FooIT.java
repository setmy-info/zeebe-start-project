package info.setmy.zeebe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FooIT {

    Foo foo;

    @Before
    public void setUp() {
        foo = new Foo();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetBar() {
        foo.setBar("Bar");
    }
}
