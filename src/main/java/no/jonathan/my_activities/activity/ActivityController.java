package no.jonathan.my_activities.activity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.jonathan.my_activities.response.Response;
import no.jonathan.my_activities.response.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final ResponseUtil responseUtil;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<Response<PagedModel<EntityModel<ActivityDto>>>> getActivities(
            Pageable pageable,
            PagedResourcesAssembler<ActivityDto> assembler,
            Authentication authentication
    ) {
        Page<ActivityDto> activities = activityService.getAllActivities(authentication, pageable);

        if (activities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responseUtil.buildSuccessResponse(assembler.toModel(activities)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<Response<ActivityDto>> createActivity(
            @Valid @RequestBody ActivityCreateRequest createRequest,
            Authentication authentication
    ) {
        ActivityDto newActivity = activityService.createActivity(authentication, createRequest);

        return ResponseEntity.ok(responseUtil.buildSuccessResponse(newActivity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping("{activityId}")
    public ResponseEntity<Response<ActivityDto>> updateActivity(
            @PathVariable("activityId") Long activityId,
            @Valid @RequestBody ActivityUpdateRequest updateRequest,
            Authentication authentication
    ) {
        ActivityDto updatedActivity = activityService.updateActivity(
                authentication,
                activityId,
                updateRequest
        );

        return ResponseEntity.ok(responseUtil.buildSuccessResponse(updatedActivity));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("{activityId}")
    public ResponseEntity<Response<Long>> deleteActivity(
            @PathVariable("activityId") Long activityId,
            Authentication authentication
    ) {
        Long deletedActivityId = activityService.deleteActivity(authentication, activityId);

        return ResponseEntity.ok(responseUtil.buildSuccessResponse(deletedActivityId));
    }

}
