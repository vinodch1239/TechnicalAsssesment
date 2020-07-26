package com.perago.techtest.test;

import com.perago.techtest.*;
import com.sun.istack.internal.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static org.junit.Assert.*;

/**
 * Created by paballo on 2017/08/16.
 */
public class DiffEngineImplTest {

    Logger logger = Logger.getLogger(DiffEngineImplTest.class);

    @Test
    public void diffEngineCalculatesDiffShouldEqualModifedWhenOriginalIsNull() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer renderer = new DiffRendererImpl();

        Person person = new Person();
        person.setFirstName("Fred");
        person.setSurname("Smith");
        Person friend = new Person();
        friend.setFirstName("Tshepo");
        person.setFriend(friend);
        Set<String> nickNames = new HashSet<>();
        nickNames.add("shane");
        nickNames.add("shawn");
        person.setNickNames(nickNames);

        Diff<Person> diff = diffEngine.calculate(null, person);
        logger.log(Level.INFO,renderer.render(diff));
        assertNotNull(diff);
        assertEquals(diff.getHolder(), person);

    }

    @Test
    public void diffEngineApplyShouldReturnModifiedWhenOriginalIsNullAndModifiedNonNull() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer diffRenderer = new DiffRendererImpl();
        Person modified = new Person();
        modified.setFirstName("Fred");
        modified.setSurname("Smith");

        Diff<Person> diff = diffEngine.calculate(null, modified);
        logger.log(Level.INFO, diffRenderer.render(diff));
        Person clone = (Person)BeanUtils.cloneBean(modified);
        clone.setSurname("Smithson");

        Person applied = diffEngine.apply(clone, diff);
        assertNotNull(applied);
        assertEquals(applied.getSurname(),"Smith");
        assertEquals(applied.getSurname(),modified.getSurname());

    }

    @Test
    public void diffEngineApplyShouldReturnNullWhenModifiedIsNull() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer renderer = new DiffRendererImpl();
        Person original = new Person();
        original.setFirstName("Fred");
        original.setSurname("Smith");

        Diff<Person> diff = diffEngine.calculate(original, null);
        logger.log(Level.INFO, renderer.render(diff));
        Person applied = diffEngine.apply(original, diff);
        assertNotNull(original);
        assertNull(applied);

    }


    @Test
    public void diffEngineApplyShouldWorkWhenModifiedAndOriginalAreNull() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer renderer = new DiffRendererImpl();
        Person original = new Person();
        original.setFirstName("Fred");
        original.setSurname("Smith");

        Person friend = new Person();
        friend.setFirstName("Tshepo");

        Person modified = (Person) BeanUtils.cloneBean(original);
        modified.setSurname("Johnson");
        modified.setFriend(friend);

        Diff<Person> diff = diffEngine.calculate(original, modified);
        logger.log(Level.INFO, renderer.render(diff));

        Person applied = diffEngine.apply(modified, diff);
        assertEquals("Smith", original.getSurname());
        assertEquals("Johnson", applied.getSurname());
        assertEquals(applied.getFriend().getFirstName(), "Tshepo");

    }

    @Test
    public void diffShouldContainChangeLogs() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer renderer = new DiffRendererImpl();
        Person person = new Person();
        person.setFirstName("Fred");
        person.setSurname("Smith");

       TestClass test = new TestClass();
       test.setName("hello");
       test.setScore(5);
       TestClass test1 = (TestClass)BeanUtils.cloneBean(test);
       test1.setScore(10);




        Diff<TestClass> diff = diffEngine.calculate(test, test1);
        logger.log(Level.INFO, renderer.render(diff));

        assertNotEquals(0L, diff.getChangeLogs().size());

    }


    @Test
    public void diffApplyShouldWorkOnCollections() throws Exception {
        DiffEngine diffEngine = new DiffEngineImpl();
        DiffRenderer renderer = new DiffRendererImpl();
        Person person = new Person();
        person.setFirstName("Fred");
        person.setSurname("Smith");
        Set<String> nickNames = new HashSet<>();
        nickNames.add("Shane");
        person.setNickNames(nickNames);

        Person clone = (Person) BeanUtils.cloneBean(person);
        clone.setFirstName("Freddie");


        Set<String> names = new HashSet<>();
        names.add("Fredzen");
        names.add("MaFred");
        names.add("Shane");
        //this is cheating I know
        clone.setNickNames(names);

        Diff<Person> diff = diffEngine.calculate(person, clone);
        logger.log(Level.INFO, renderer.render(diff));
        Person applied = diffEngine.apply(person, diff);
        assertEquals(applied.getFirstName(),clone.getFirstName());
        assertEquals(applied.getNickNames(),clone.getNickNames());

        assertEquals(applied, clone);



    }




}
