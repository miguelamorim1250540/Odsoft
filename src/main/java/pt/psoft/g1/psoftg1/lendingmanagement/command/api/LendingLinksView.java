package pt.psoft.g1.psoftg1.lendingmanagement.command.api;

import lombok.Data;
import java.util.Map;

@Data
public class LendingLinksView {
    private Map<String, String> self;
    private Map<String, String> book;
    private Map<String, String> reader;
}
