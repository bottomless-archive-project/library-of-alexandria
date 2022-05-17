import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ApplicationInfoEntity} from "../../shared/application/domain/application-info-entity";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard-applications.component.html',
  styleUrls: ['./dashboard-applications.component.scss']
})
export class DashboardApplicationsComponent implements OnInit {

  applications: ApplicationInfoEntity;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.applications = this.route.snapshot.data.applications;
  }
}
