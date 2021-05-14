import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  statistics: any;
  searchText: string = '';
  modelChanged: Subject<string> = new Subject<string>();
  languages: string[][];
  language: any = undefined;
  fileTypes: any = {
    PDF: false,
    DOC: false,
    PPT: false,
    RTF: false,
    XLS: false,
    DOCX: false,
    XLSX: false,
    EPUB: false,
    MOBI: false
  };

  constructor(private route: ActivatedRoute) {
    this.modelChanged.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(searchText => {
        console.log(searchText);

        return this.searchText = searchText;
      });

    this.languages = [
      ['af', 'afrikaans'],
      ['sq', 'albanian'],
      ['ar', 'arabic'],
      ['hy', 'armenian'],
      ['az', 'azerbaijani'],
      ['eu', 'basque'],
      ['be', 'belarusian'],
      ['bn', 'bengali'],
      ['nb', 'bokmal'],
      ['bs', 'bosnian'],
      ['bg', 'bulgarian'],
      ['ca', 'catalan'],
      ['zh', 'chinese'],
      ['hr', 'croatian'],
      ['cs', 'czech'],
      ['da', 'danish'],
      ['nl', 'dutch'],
      ['en', 'english'],
      ['eo', 'esperanto'],
      ['et', 'estonian'],
      ['fi', 'finnish'],
      ['fr', 'french'],
      ['lg', 'ganda'],
      ['ka', 'georgian'],
      ['de', 'german'],
      ['el', 'greek'],
      ['gu', 'gujarati'],
      ['he', 'hebrew'],
      ['hi', 'hindi'],
      ['hu', 'hungarian'],
      ['is', 'icelandic'],
      ['id', 'indonesian'],
      ['ga', 'irish'],
      ['it', 'italian'],
      ['ja', 'japanese'],
      ['kk', 'kazakh'],
      ['ko', 'korean'],
      ['la', 'latin'],
      ['lv', 'latvian'],
      ['lt', 'lithuanian'],
      ['mk', 'macedonian'],
      ['ms', 'malay'],
      ['mr', 'marathi'],
      ['mn', 'mongolian'],
      ['nn', 'nynorsk'],
      ['fa', 'persian'],
      ['pl', 'polish'],
      ['pt', 'portuguese'],
      ['pa', 'punjabi'],
      ['ro', 'romanian'],
      ['ru', 'russian'],
      ['sr', 'serbian'],
      ['sn', 'shona'],
      ['sk', 'slovak'],
      ['sl', 'slovene'],
      ['so', 'somali'],
      ['st', 'sotho'],
      ['es', 'spanish'],
      ['sw', 'swahili'],
      ['sv', 'swedish'],
      ['tl', 'tagalog'],
      ['ta', 'tamil'],
      ['te', 'telugu'],
      ['th', 'thai'],
      ['ts', 'tsonga'],
      ['tn', 'tswana'],
      ['tr', 'turkish'],
      ['uk', 'ukrainian'],
      ['ur', 'urdu'],
      ['vi', 'vietnamese'],
      ['cy', 'welsh'],
      ['xh', 'xhosa'],
      ['yo', 'yoruba'],
      ['zu', 'zulu']
    ]
  }

  ngOnInit(): void {
    this.statistics = this.route.snapshot.data.statistics;
  }

  changed(text: string) {
    this.modelChanged.next(text);
  }

  setLanguage(language: any) {
    this.language = language;

    this.refreshHits();
  };

  refreshHits() {
    console.log("Should refresh hits here!", this.fileTypes);
  }
}
