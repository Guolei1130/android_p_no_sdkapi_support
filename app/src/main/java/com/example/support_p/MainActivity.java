package com.example.support_p;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

  // Used to load the 'native-lib' library on application startup.
  static {
    System.loadLibrary("substrate");
    System.loadLibrary("test2");
    System.loadLibrary("native-lib");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Example of a call to a native method
    TextView tv = (TextView) findViewById(R.id.sample_text);
    tv.setText(stringFromJNI());

    ReflectionHelper.testMethod();
    ReflectionHelper.testField();
    findViewById(R.id.test_method).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          Class clz = Class.forName("android.app.ActivityThread");
          Method method = clz.getDeclaredMethod("getIntentBeingBroadcast");
          method.setAccessible(true);
          method.invoke(null);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    findViewById(R.id.test_field).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          Class clz = Class.forName("android.app.ActivityThread");
          Field field = clz.getDeclaredField("TAG");
          field.setAccessible(true);
          field.get(null);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    findViewById(R.id.reflection_helper).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        testJavaPojie();
      }
    });

    findViewById(R.id.test_reflection_helper).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ReflectionHelper.testMethod();
        ReflectionHelper.testField();
      }
    });
  }

  private void testJavaPojie() {
    try {
      Class reflectionHelperClz = Class.forName("com.example.support_p.ReflectionHelper");
      Class classClz = Class.class;
      Field classLoaderField = classClz.getDeclaredField("classLoader");
      classLoaderField.setAccessible(true);
      classLoaderField.set(reflectionHelperClz, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  public native String stringFromJNI();
}
