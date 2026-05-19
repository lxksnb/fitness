package com.fitness.controller;

import com.fitness.common.Result;
import com.fitness.dto.ActionDTO;
import com.fitness.entity.ActionLibrary;
import com.fitness.entity.UserActionRecord;
import com.fitness.service.ActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    public Result<List<ActionLibrary>> search(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String suitableFor) {
        return Result.ok(actionService.search(keyword, suitableFor));
    }

    @PostMapping
    public Result<ActionLibrary> create(@RequestBody ActionDTO dto) {
        return Result.ok(actionService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ActionDTO dto) {
        actionService.update(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        actionService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/records")
    public Result<List<UserActionRecord>> getRecords(@PathVariable Long id) {
        return Result.ok(actionService.getRecords(id));
    }
}
