package org.example;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TimeZone;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // Отримання параметра timezone з запиту
        String timezoneParam = req.getParameter("timezone");

        // Валідація часового поясу
        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            try {
                // Перевірка наявності та коректності часового поясу за допомогою TimeZone
                TimeZone timeZone = TimeZone.getTimeZone(timezoneParam);
                // Якщо TimeZone.getTimeZone повертає "GMT" або час у мілісекундах, це некоректний часовий пояс
                if (timeZone.getID().equals("GMT") && !timezoneParam.equals("GMT")) {
                    throw new IllegalArgumentException();
                }
                // Якщо вказаний часовий пояс некоректний, викидаємо виняток
            } catch (IllegalArgumentException e) {
                // Встановлення статусу відповіді на 400 для некоректного запиту
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // Повернення повідомлення "Invalid timezone"
                resp.setContentType("text/html");
                resp.getWriter().write("Invalid timezone");
                return;
            }
        }

        // Продовження ланцюжка фільтрації, якщо параметр timezone є коректним або його немає
        chain.doFilter(req, resp);
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Ініціалізація фільтра
    }

    @Override
    public void destroy() {
        // Звільнення ресурсів фільтра
    }

}
