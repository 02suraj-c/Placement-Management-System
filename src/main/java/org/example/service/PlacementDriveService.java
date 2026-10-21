package org.example.service;

import org.example.model.PlacementDrive;

import java.util.List;

/**
 * Service managing placement drives.
 */
public interface PlacementDriveService {
    PlacementDrive createDrive(PlacementDrive drive);
    PlacementDrive getDriveById(String driveId);
    List<PlacementDrive> getAllDrives();
    List<PlacementDrive> getDrivesByCompany(String companyId);
    PlacementDrive updateDrive(PlacementDrive drive);
    PlacementDrive cancelDrive(String driveId);
}
