//package com.auca_hr.AUCA_HR_System.services;
//
//@Service
//public class DashboardService {
//
//    @Autowired
//    private WorkerRepository workerRepository;
//
//    @Autowired
//    private AttendanceRepository attendanceRepository;
//
//    public DashboardStatsDTO getDashboardStats() {
//        DashboardStatsDTO stats = new DashboardStatsDTO();
//
//        // Basic worker statistics
//        stats.setTotalWorkers((int) workerRepository.count());
//        stats.setFullTimeWorkers((int) workerRepository.countByWorkerType(WorkerType.FULL_TIME));
//        stats.setPartTimeWorkers((int) workerRepository.countByWorkerType(WorkerType.PART_TIME));
//        stats.setInternationalWorkers((int) workerRepository.countByIsInternational(true));
//        stats.setLeaveTakers((int) workerRepository.countWorkersOnLeave());
//
//        // New workers (hired in the last 30 days)
//        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
//        stats.setNewWorkers((int) workerRepository.countNewWorkers(thirtyDaysAgo));
//
//        // Attendance statistics
//        stats.setAttendanceStats(getAttendanceStats());
//
//        // Education statistics
//        stats.setEducationStats(getEducationStats());
//
//        return stats;
//    }