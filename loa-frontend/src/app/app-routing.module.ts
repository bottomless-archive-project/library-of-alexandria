import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {SearchComponent} from "./search/search.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {SearchResolver} from "./shared/search/resolver/search.resolver";
import {StatisticsResolver} from "./statistics.resolver";

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'search', component: SearchComponent, resolve: {statistics: SearchResolver}},
  {path: 'dashboard', component: DashboardComponent, resolve: {statistics: StatisticsResolver}}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
