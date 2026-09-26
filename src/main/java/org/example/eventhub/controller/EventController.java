package org.example.eventhub.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.ApiResponse;
import org.example.eventhub.domain.Event;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.dto.EventQueryRequest;
import org.example.eventhub.service.EventService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    //创建活动
    @PostMapping
    public ApiResponse<Long> createEvent(@Valid @RequestBody EventCreateRequest eventCreateRequest) {
        //调用service层的创建活动方法
        Long eventId = eventService.createEvent(eventCreateRequest);
        return ApiResponse.success(eventId);
    }

    //查询活动
    @GetMapping("/{id}")
    public ApiResponse<Event> getById(@PathVariable Long id) {
        //调用service层的查询活动方法
        Event event = eventService.getEventById(id);
        return ApiResponse.success(event);
    }

    @GetMapping
    public ApiResponse<Page<Event>> page(EventQueryRequest request) {
        return ApiResponse.success(eventService.pageEvents(request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        eventService.publishEvent(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ApiResponse.success();
    }
}
