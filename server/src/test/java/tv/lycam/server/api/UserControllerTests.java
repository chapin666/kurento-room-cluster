package tv.lycam.server.api;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tv.lycam.server.RoomServerApplication;
import tv.lycam.server.api.module.user.UserController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by lycamandroid on 2017/6/20.
 */

@SpringBootTest(
        classes = RoomServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@WebMvcTest(value = UserController.class)
public class UserControllerTests {


    @Autowired
    private MockMvc mvc;


    @Test
    public void testExample() throws Exception {
        this.mvc.perform(post("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Honda Civic"));
    }


}
