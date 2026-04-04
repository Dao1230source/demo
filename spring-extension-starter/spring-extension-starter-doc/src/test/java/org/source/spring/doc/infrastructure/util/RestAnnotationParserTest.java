package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.RestDocElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestAnnotationParserTest {

    private RestAnnotationParser parser;

    @BeforeEach
    void setUp() {
        parser = new RestAnnotationParser();
    }

    @Test
    void testParseGetMapping() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @GetMapping("/users")
                public List<User> getUsers() {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("GET", endpoints.get(0).getHttpMethod());
        assertEquals("/users", endpoints.get(0).getPath());
    }

    @Test
    void testParsePostMapping() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @PostMapping("/users")
                public User createUser(@RequestBody User user) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("POST", endpoints.get(0).getHttpMethod());
    }

    @Test
    void testParsePutMapping() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @PutMapping("/users/{id}")
                public User updateUser(@PathVariable Long id, @RequestBody User user) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("PUT", endpoints.get(0).getHttpMethod());
    }

    @Test
    void testParseDeleteMapping() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @DeleteMapping("/users/{id}")
                public void deleteUser(@PathVariable Long id) {
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("DELETE", endpoints.get(0).getHttpMethod());
    }

    @Test
    void testParseWithRequestMapping() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api")
            public class UserController {
                @GetMapping("/users")
                public List<User> getUsers() {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("/api/users", endpoints.get(0).getPath());
    }

    @Test
    void testParseWithPathVariable() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @GetMapping("/users/{id}")
                public User getUser(@PathVariable Long id) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("id", endpoints.get(0).getPathVariables()[0]);
    }

    @Test
    void testParseWithRequestParam() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @GetMapping("/users")
                public List<User> searchUsers(@RequestParam String name) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("name", endpoints.get(0).getRequestParams()[0]);
    }

    @Test
    void testParseWithRequestBody() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @PostMapping("/users")
                public User create(@RequestBody UserRequest request) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("UserRequest", endpoints.get(0).getRequestBody());
    }

    @Test
    void testParseMultipleMethods() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                @GetMapping("/users")
                public List<User> getUsers() { return null; }
                
                @PostMapping("/users")
                public User createUser() { return null; }
                
                @DeleteMapping("/users/{id}")
                public void deleteUser() {}
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(3, endpoints.size());
    }

    @Test
    void testParseWithJavadoc() {
        String sourceCode = """
            package org.example;
            
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            public class UserController {
                /**
                 * 获取用户详情
                 */
                @GetMapping("/users/{id}")
                public User getUser(@PathVariable Long id) {
                    return null;
                }
            }
            """;
        
        List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "org.example.UserController");
        
        assertEquals(1, endpoints.size());
        assertEquals("获取用户详情", endpoints.get(0).getDocContent());
    }
}