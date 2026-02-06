package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchMarkReadCmd {
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;
}
