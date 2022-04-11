import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ApplicationInfoEntity} from "../applications/domain/application-info-entity";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  statistics: any;
  applications: ApplicationInfoEntity;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.statistics = this.route.snapshot.data.statistics;
    this.applications = this.route.snapshot.data.applications;
  }

  keepOrder(a: any, b: any): any {
    return a;
  }
}
