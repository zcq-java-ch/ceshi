package com.hxls.appointment.controller;

import com.hxls.appointment.service.TAppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("appointment/audit")
@Tag(name = "预约审核")
@AllArgsConstructor
public class AppointmentAuditController {


    private final TAppointmentService tAppointmentService;




}
