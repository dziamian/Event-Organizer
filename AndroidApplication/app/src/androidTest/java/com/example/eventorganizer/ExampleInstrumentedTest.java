package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.RequiresFeature;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;

import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.eventorganizer", appContext.getPackageName());
    }

    @Test
    public void ensureLoginFormIsActive() {
        MainActivity mainActivity = mainActivityTestRule.getActivity();
        View loginText = mainActivity.findViewById(R.id.loginText);
        assertThat(loginText, notNullValue());
        assertThat(loginText, instanceOf(EditText.class));
    }

    @Test
    public void testLogin() {
        Espresso.onView(withId(R.id.loginText)).perform(typeText("dziamian"));
        Espresso.onView(withId(R.id.passwordText)).perform(typeText("123"));
        Espresso.closeSoftKeyboard();
        Intents.init();
        Espresso.onView(withId(R.id.loginBtn)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }
}
