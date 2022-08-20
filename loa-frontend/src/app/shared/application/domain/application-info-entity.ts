import {AdministratorApplicationInstance} from "./administrator-application-instance";
import {DownloaderApplicationInstance} from "./downloader-application-instance";
import {GeneratorApplicationInstance} from "./generator-application-instance";
import {IndexerApplicationInstance} from "./indexer-application-instance";
import {QueueApplicationInstance} from "./queue-application-instance";
import {VaultApplicationInstance} from "./vault-application-instance";
import {StagingApplicationInstance} from "./vault-pplication-instance";

export class ApplicationInfoEntity {

  administrators: AdministratorApplicationInstance[];
  downloaders: DownloaderApplicationInstance[];
  generators: GeneratorApplicationInstance[];
  indexers: IndexerApplicationInstance[];
  queues: QueueApplicationInstance[];
  vaults: VaultApplicationInstance[];
  stagings: StagingApplicationInstance[];
}
