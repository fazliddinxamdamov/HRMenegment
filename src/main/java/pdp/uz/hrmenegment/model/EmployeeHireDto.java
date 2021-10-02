package pdp.uz.hrmenegment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHireDto {
    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private Double salary;

    @NotNull
    @Email
    private String email;

    private Integer companyId;
}
