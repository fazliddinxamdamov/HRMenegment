package pdp.uz.hrmenegment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pdp.uz.hrmenegment.entity.Employee;
import pdp.uz.hrmenegment.entity.EmployeeSalary;
import pdp.uz.hrmenegment.entity.Task;
import pdp.uz.hrmenegment.entity.TourniquetHistory;
import pdp.uz.hrmenegment.model.*;
import pdp.uz.hrmenegment.repository.EmployeeRepository;
import pdp.uz.hrmenegment.repository.SalaryRepository;
import pdp.uz.hrmenegment.repository.TaskRepository;
import pdp.uz.hrmenegment.repository.TourniquetHistoryRepository;
import pdp.uz.hrmenegment.security.JwtProvider;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class EmployeeService implements UserDetailsService {
    @Autowired
    SalaryRepository salaryRepository;
    @Autowired
    TourniquetHistoryRepository tourniquetHistoryRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    HireService hireService;

    public ApiResponse login(EmployeeLoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
            Employee employee = (Employee) authentication.getPrincipal();

            String token = jwtProvider.generateToken(employee.getEmail(), employee.getRole());

            List<Task> tasks =
                    taskRepository.findAllByDeadlineBeforeAndEmployee_EmailAndStatus(Date.valueOf(LocalDate.now()), employee.getEmail(), 2);
            for (Task task : tasks) {
                task.setStatus(4);
                taskRepository.save(task);
            }
            tasks =
                    taskRepository.findAllByDeadlineBeforeAndEmployee_EmailAndStatus(Date.valueOf(LocalDate.now()), employee.getEmail(), 1);
            for (Task task : tasks) {
                task.setStatus(4);
                taskRepository.save(task);
            }
            return new ApiResponse("Token", true, token);
        } catch (BadCredentialsException e) {
            return new ApiResponse("Login or password is incorrect", false);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
    }


    public ApiResponse edit(EmployeeDto email, String id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(UUID.fromString(id));
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);
        Employee employee = optionalEmployee.get();
        employee.setEmail(email.getEmail());
        employee.setEnabled(false);
        employee.setEmailCode(UUID.randomUUID().toString());
        Employee savedEmployee = employeeRepository.save(employee);
        hireService.sendEmail(savedEmployee.getEmail(), savedEmployee.getEmailCode());
        return new ApiResponse("Employee updated", true);
    }

    public ApiResponse editSalary(ChangeSalaryDto dto) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEnabledTrue(dto.getEmail());
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);

        Employee employee = optionalEmployee.get();
        employee.setSalary(dto.getSalary());
        employeeRepository.save(employee);
        return new ApiResponse("Salary updated", true);
    }

    public ApiResponse get() {
        Employee employee = getEmployee();
        if (employee == null)
            return new ApiResponse("Error", false);
        List<Employee> employees = employeeRepository.findAllByCompanyIdAndEnabledTrue(employee.getCompany().getId());
        return new ApiResponse("Completed", true, employees);
    }

    public Employee getEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            return (Employee) authentication.getPrincipal();
        }
        return null;
    }

    public ApiResponse info(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);
        try {
            Employee employee = optionalEmployee.get();
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");

            InfoDto infoDto = new InfoDto();
            infoDto.setEmail(employee.getEmail());
            infoDto.setFirstName(employee.getFirstName());
            infoDto.setLastName(employee.getLastName());

            List<TourniquetHistory> histories = tourniquetHistoryRepository.findAllByExitedAtBetween(fromDate, toDate);
            infoDto.setHistories(histories);

            histories.addAll(tourniquetHistoryRepository.findAllByEnteredAtBetween(fromDate,toDate));

            List<Task> tasks = taskRepository.findAllByCompletedAtBetweenAndEmployee_Email(fromDate, toDate, email);
            infoDto.setTasks(tasks);

            return new ApiResponse("OK", true, infoDto);
        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse infoSalary(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);
        try {
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");

            List<EmployeeSalary> salaries = salaryRepository.findAllByUpdatedAtBetweenAndEmployeeId(fromDate, toDate, optionalEmployee.get().getId());

            return new ApiResponse("OK", true, salaries);
        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse infoTask(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);
        try {
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");

            List<Task> completedTasks = taskRepository.findAllByCompletedAtBetweenAndEmployee_Email(fromDate, toDate, email);
            List<Task> uncompletedTasks
                    = taskRepository.findAllByCreatedAtBetweenAndStatusAndEmployee_Email(fromDate, toDate, 4, email);
            completedTasks.addAll(uncompletedTasks);
            return new ApiResponse("OK", true, completedTasks);
        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse delete(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent())
            return new ApiResponse("Employee not found", false);
        Employee employee = optionalEmployee.get();
        employee.setEnabled(false);
        employeeRepository.save(employee);
        return new ApiResponse("Employee deleted", true);
    }
}
