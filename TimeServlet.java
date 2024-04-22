package org.example;


import org.thymeleaf.TemplateEngine;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;


@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final String LAST_TIMEZONE_COOKIE = "lastTimezone";
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        templateEngine = new TemplateEngine();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(getServletContext());
        templateResolver.setPrefix("./templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String timezoneParam = req.getParameter("timezone");
        ZoneId zoneId;

        // Перевірка параметра timezone
        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            try {
                zoneId = ZoneId.of(timezoneParam);
                // Збереження валідного часового поясу в Cookie
                Cookie timezoneCookie = new Cookie(LAST_TIMEZONE_COOKIE, timezoneParam);
                timezoneCookie.setPath("/");
                resp.addCookie(timezoneCookie);
            } catch (Exception e) {
                // Некоректний часовий пояс, fallback до UTC
                zoneId = ZoneId.of("UTC");
            }
        } else {
            // Якщо параметр timezone не надано, отримаємо часовий пояс з Cookie
            zoneId = getTimeZoneFromCookies(req);
            if (zoneId == null) {
                // Якщо немає валідного часового поясу, fallback до UTC
                zoneId = ZoneId.of("UTC");
            }
        }

        // Обчислення поточного часу
        LocalDateTime currentTime = LocalDateTime.now(zoneId);
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));

        // Створити контекст для рендерингу
        WebContext context = new WebContext(req, resp, getServletContext(), req.getLocale());
        context.setVariable("timeZone", zoneId.getId());
        context.setVariable("formattedTime", formattedTime);

        // Використання шаблонізатора для рендерингу
        templateEngine.process("time", context, resp.getWriter());
    }
    private ZoneId getTimeZoneFromCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (LAST_TIMEZONE_COOKIE.equals(cookie.getName())) {
                    try {
                        return ZoneId.of(cookie.getValue());
                    } catch (Exception e) {
                        // Некоректний часовий пояс у cookie, повертаємо null
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
