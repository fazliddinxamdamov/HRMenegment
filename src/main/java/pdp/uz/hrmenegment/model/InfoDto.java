package pdp.uz.hrmenegment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pdp.uz.hrmenegment.entity.Task;
import pdp.uz.hrmenegment.entity.TourniquetHistory;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoDto {
    private String firstName;
    private String lastName;
    private String email;
    private List<Task> tasks;
    private List<TourniquetHistory> histories;
}
