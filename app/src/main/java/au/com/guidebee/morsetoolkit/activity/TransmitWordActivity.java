package au.com.guidebee.morsetoolkit.activity;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class TransmitWordActivity extends MorseActivity {

    private final static String STATE_TOTAL_WORDS = "State_Total_Words";
    private final static String STATE_TOTAL_CORRECT = "State_Total_Correct";
    private final static String STATE_CURRENT_WORD = "State_Current_Word";
    private final static String STATE_RESPONSE_WORD = "State_Response_Word";
    private final static String STATE_RESPONSE_WORD_INDEX = "State_Response_Word_Index";
    private final Random random = new Random();
    protected TextView textViewWord;
    protected TextView textViewResponse;
    protected TextView textViewTryInfo;
    protected ImageButton imageButtonPlay;
    protected Button buttonNext;
    protected String currentWord = "guidebee";
    protected int currentWorldIndex = 0;
    protected String responseWord = "";
    private int totalWords = 0;
    private int totalCorrect = 0;
    private ArrayList<String> wordList = new ArrayList<>();

    @Override
    public void onEmit(Character character) {
        textViewWord.post(() -> {
            if (!(character == ' ' && lastChar == ' ')) {
                char ch = currentWord.charAt(currentWorldIndex);
                if (character.toString().compareToIgnoreCase(String.valueOf(ch)) == 0) {
                    setWordWithColor(true, String.valueOf(character));
                    responseWord += ch;
                    currentWorldIndex += 1;
                } else {
                    setWordWithColor(false, String.valueOf(character));
                    onErrorAnswer();
                }

                if (currentWorldIndex >= currentWord.length()) {
                    onCorrectAnswer();
                    textViewWord.setTextColor(correctAnswerColor);
                    textViewWord.postDelayed(() -> randomWord(null), 1500);
                    totalCorrect += 1;
                }

            }
            textViewTryInfo.setText(totalCorrect + "/" + totalWords);

        });
        lastChar = character;
    }


    private void setWordWithColor(boolean ok, String character) {
        String strText = responseWord + String.valueOf(character).toUpperCase();
        int startIndex = 0;
        int endIndex = responseWord.length();
        SpannableString styledString = new SpannableString(strText);
        // make the text twice as large
        styledString.setSpan(new ForegroundColorSpan(correctAnswerColor), startIndex, endIndex,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        // make text bold
        int lastCharIndex = strText.length();
        if (ok) {
            styledString.setSpan(new ForegroundColorSpan(correctAnswerColor), lastCharIndex - 1,
                    lastCharIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            styledString.setSpan(new ForegroundColorSpan(wrongAnswerColor), lastCharIndex - 1,
                    lastCharIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        textViewResponse.setText(styledString);
    }

    @Override
    protected void setActivityTitle() {
        actionBar.setTitle(R.string.transmit_word_title);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_CURRENT_WORD, currentWord);
        savedInstanceState.putInt(STATE_TOTAL_WORDS, totalWords);
        savedInstanceState.putInt(STATE_TOTAL_CORRECT, totalCorrect);
        savedInstanceState.putString(STATE_RESPONSE_WORD, responseWord);
        savedInstanceState.putInt(STATE_RESPONSE_WORD_INDEX, currentWorldIndex);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_transmit_word);
        super.onCreate(savedInstanceState);
        textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewResponse = (TextView) findViewById(R.id.textViewResponse);
        textViewTryInfo = (TextView) findViewById(R.id.textViewTryInfo);
        buttonNext = (Button) findViewById(R.id.buttonNext);

        String morseText = "Morse code is a method of transmitting text information " +
                "as a series of on-off tones, lights, or clicks that can be directly understood by " +
                "a skilled listener or observer without special equipment. The International Morse Code " +
                "encodes the ISO basic Latin alphabet, some extra Latin letters, the Arabic numerals " +
                "and a small set of punctuation and procedural signals (prosigns) as standardized sequences " +
                "of short and long signals called dots and dashes, or dits and dahs, as in amateur radio practice." +
                " Because many non-English natural languages use more than the 26 Roman letters, extensions " +
                "to the Morse alphabet exist for those languages.\n" +
                "\n" + " Guidebee , Perth, Australia " +
                "Each Morse code symbol represents either a text character (letter or numeral) or a " +
                "prosign and is represented by a unique sequence of dots and dashes. The duration of" +
                " a dash is three times the duration of a dot. Each dot or dash is followed by a " +
                "short silence, equal to the dot duration. The letters of a word are separated by a " +
                "space equal to three dots (one dash), and the words are separated by a space equal to " +
                "seven dots. The dot duration is the basic unit of time measurement in code transmission. " +
                "To increase the speed of the communication, the code was designed so that the length of " +
                "each character in Morse varies approximately inversely to its frequency of occurrence " +
                "in English. Thus the most common letter in English, the letter E, has the shortest code," +
                " a single dot.\n" + " Guidebee , Perth, Australia " +
                "\n" +
                "Morse code is used by some amateur radio operators, although knowledge" +
                " of and proficiency with it is no longer required for licensing in most " +
                "countries. Pilots and air traffic controllers usually need only a cursory " +
                "understanding. Aeronautical navigational aids, such as VORs and NDBs, " +
                "constantly identify in Morse code. Compared to voice, Morse code is less" +
                " sensitive to poor signal conditions, yet still comprehensible to humans " +
                "without a decoding device. Morse is therefore a useful alternative to synthesized" +
                " speech for sending automated data to skilled listeners on voice channels. " +
                "Many amateur radio repeaters, for example, identify with Morse, even though " +
                "they are used for voice communications. Guidebee , Perth, Australia";
        String word_3000="a  abandon  ability  able  abortion  about  above  abroad  absence  absolute  absolutely  absorb  abuse  academic  accept  access  accident  accompany  accomplish  according  account  accurate  accuse  achieve  achievement  acid  acknowledge  acquire  across  act  action  active  activist  activity  actor  actress  actual  actually  ad  adapt  add  addition  additional  address  adequate  adjust  adjustment  administration  administrator  admire  admission  admit  adolescent  adopt  adult  advance  advanced  advantage  adventure  advertising  advice  advise  adviser  advocate  affair  affect  afford  afraid  African  African-American  after  afternoon  again  against  age  agency  agenda  agent  aggressive  ago  agree  agreement  agricultural  ah  ahead  aid  aide  AIDS  aim  air  aircraft  airline  airport  album  alcohol  alive  all  alliance  allow  ally  almost  alone  along  already  also  alter  alternative  although  always  AM  amazing  American  among  amount  analysis  analyst  analyze  ancient  and  anger  angle  angry  animal  anniversary  announce  annual  another  answer  anticipate  anxiety  any  anybody  anymore  anyone  anything  anyway  anywhere  apart  apartment  apparent  apparently  appeal  appear  appearance  apple  application  apply  appoint  appointment  appreciate  approach  appropriate  approval  approve  approximately  Arab  architect  area  argue  argument  arise  arm  armed  army  around  arrange  arrangement  arrest  arrival  arrive  art  article  artist  artistic  as  Asian  aside  ask  asleep  aspect  assault  assert  assess  assessment  asset  assign  assignment  assist  assistance  assistant  associate  association  assume  assumption  assure  at  athlete  athletic  atmosphere  attach  attack  attempt  attend  attention  attitude  attorney  attract  attractive  attribute  audience  author  authority  auto  available  average  avoid  award  aware  awareness  away  awful  baby  back  background  bad  badly  bag  bake  balance  ball  ban  band  bank  bar  barely  barrel  barrier  base  baseball  basic  basically  basis  basket  basketball  bathroom  battery  battle  be  beach  bean  bear  beat  beautiful  beauty  because  become  bed  bedroom  beer  before  begin  beginning  behavior  behind  being  belief  believe  bell  belong  below  belt  bench  bend  beneath  benefit  beside  besides  best  bet  better  between  beyond  Bible  big  bike  bill  billion  bind  biological  bird  birth  birthday  bit  bite  black  blade  blame  blanket  blind  block  blood  blow  blue  board  boat  body  bomb  bombing  bond  bone  book  boom  boot  border  born  borrow  boss  both  bother  bottle  bottom  boundary  bowl  box  boy  boyfriend  brain  branch  brand  bread  break  breakfast  breast  breath  breathe  brick  bridge  brief  briefly  bright  brilliant  bring  British  broad  broken  brother  brown  brush  buck  budget  build  building  bullet  bunch  burden  burn  bury  bus  business  busy  but  butter  button  buy  buyer  by  cabin  cabinet  cable  cake  calculate  call  camera  camp  campaign  campus  can  Canadian  cancer  candidate  cap  capability  capable  capacity  capital  captain  capture  car  carbon  card  care  career  careful  carefully  carrier  carry  case  cash  cast  cat  catch  category  Catholic  cause  ceiling  celebrate  celebration  celebrity  cell  center  central  century  CEO  ceremony  certain  certainly  chain  chair  chairman  challenge  chamber  champion  championship  chance  change  changing  channel  chapter  character  characteristic  characterize  charge  charity  chart  chase  cheap  check  cheek  cheese  chef  chemical  chest  chicken  chief  child  childhood  Chinese  chip  chocolate  choice  cholesterol  choose  Christian  Christmas  church  cigarette  circle  circumstance  cite  citizen  city  civil  civilian  claim  class  classic  classroom  clean  clear  clearly  client  climate  climb  clinic  clinical  clock  close  closely  closer  clothes  clothing  cloud  club  clue  cluster  coach  coal  coalition  coast  coat  code  coffee  cognitive  cold  collapse  colleague  collect  collection  collective  college  colonial  color  column  combination  combine  come  comedy  comfort  comfortable  command  commander  comment  commercial  commission  commit  commitment  committee  common  communicate  communication  community  company  compare  comparison  compete  competition  competitive  competitor  complain  complaint  complete  completely  complex  complicated  component  compose  composition  comprehensive  computer  concentrate  concentration  concept  concern  concerned  concert  conclude  conclusion  concrete  condition  conduct  conference  confidence  confident  confirm  conflict  confront  confusion  Congress  congressional  connect  connection  consciousness  consensus  consequence  conservative  consider  considerable  consideration  consist  consistent  constant  constantly  constitute  constitutional  construct  construction  consultant  consume  consumer  consumption  contact  contain  container  contemporary  content  contest  context  continue  continued  contract  contrast  contribute  contribution  control  controversial  controversy  convention  conventional  conversation  convert  conviction  convince  cook  cookie  cooking  cool  cooperation  cop  cope  copy  core  corn  corner  corporate  corporation  correct  correspondent  cost  cotton  couch  could  council  counselor  count  counter  country  county  couple  courage  course  court  cousin  cover  coverage  cow  crack  craft  crash  crazy  cream  create  creation  creative  creature  credit  crew  crime  criminal  crisis  criteria  critic  critical  criticism  criticize  crop  cross  crowd  crucial  cry  cultural  culture  cup  curious  current  currently  curriculum  custom  customer  cut  cycle  dad  daily  damage  dance  danger  dangerous  dare  dark  darkness  data  date  daughter  day  dead  deal  dealer  dear  death  debate  debt  decade  decide  decision  deck  declare  decline  decrease  deep  deeply  deer  defeat  defend  defendant  defense  defensive  deficit  define  definitely  definition  degree  delay  deliver  delivery  demand  democracy  Democrat  democratic  demonstrate  demonstration  deny  department  depend  dependent  depending  depict  depression  depth  deputy  derive  describe  description  desert  deserve  design  designer  desire  desk  desperate  despite  destroy  destruction  detail  detailed  detect  determine  develop  developing  development  device  devote  dialogue  die  diet  differ  difference  different  differently  difficult  difficulty  dig  digital  dimension  dining  dinner  direct  direction  directly  director  dirt  dirty  disability  disagree  disappear  disaster  discipline  discourse  discover  discovery  discrimination  discuss  discussion  disease  dish  dismiss  disorder  display  dispute  distance  distant  distinct  distinction  distinguish  distribute  distribution  district  diverse  diversity  divide  division  divorce  DNA  do  doctor  document  dog  domestic  dominant  dominate  door  double  doubt  down  downtown  dozen  draft  drag  drama  dramatic  dramatically  draw  drawing  dream  dress  drink  drive  driver  drop  drug  dry  due  during  dust  duty  each  eager  ear  early  earn  earnings  earth  ease  easily  east  eastern  easy  eat  economic  economics  economist  economy  edge  edition  editor  educate  education  educational  educator  effect  effective  effectively  efficiency  efficient  effort  egg  eight  either  elderly  elect  election  electric  electricity  electronic  element  elementary  eliminate  elite  else  elsewhere  e-mail  embrace  emerge  emergency  emission  emotion  emotional  emphasis  emphasize  employ  employee  employer  employment  empty  enable  encounter  encourage  end  enemy  energy  enforcement  engage  engine  engineer  engineering  English  enhance  enjoy  enormous  enough  ensure  enter  enterprise  entertainment  entire  entirely  entrance  entry  environment  environmental  episode  equal  equally  equipment  era  error  escape  especially  essay  essential  essentially  establish  establishment  estate  estimate  etc  ethics  ethnic  European  evaluate  evaluation  even  evening  event  eventually  ever  every  everybody  everyday  everyone  everything  everywhere  evidence  evolution  evolve  exact  exactly  examination  examine  example  exceed  excellent  except  exception  exchange  exciting  executive  exercise  exhibit  exhibition  exist  existence  existing  expand  expansion  expect  expectation  expense  expensive  experience  experiment  expert  explain  explanation  explode  explore  explosion  expose  exposure  express  expression  extend  extension  extensive  extent  external  extra  extraordinary  extreme  extremely  eye  fabric  face  facility  fact  factor  factory  faculty  fade  fail  failure  fair  fairly  faith  fall  false  familiar  family  famous  fan  fantasy  far  farm  farmer  fashion  fast  fat  fate  father  fault  favor  favorite  fear  feature  federal  fee  feed  feel  feeling  fellow  female  fence  few  fewer  fiber  fiction  field  fifteen  fifth  fifty  fight  fighter  fighting  figure  file  fill  film  final  finally  finance  financial  find  finding  fine  finger  finish  fire  firm  first  fish  fishing  fit  fitness  five  fix  flag  flame  flat  flavor  flee  flesh  flight  float  floor  flow  flower  fly  focus  folk  follow  following  food  foot  football  for  force  foreign  forest  forever  forget  form  formal  formation  former  formula  forth  fortune  forward  found  foundation  founder  four  fourth  frame  framework  free  freedom  freeze  French  frequency  frequent  frequently  fresh  friend  friendly  friendship  from  front  fruit  frustration  fuel  full  fully  fun  function  fund  fundamental  funding  funeral  funny  furniture  furthermore  future  gain  galaxy  gallery  game  gang  gap  garage  garden  garlic  gas  gate  gather  gay  gaze  gear  gender  gene  general  generally  generate  generation  genetic  gentleman  gently  German  gesture  get  ghost  giant  gift  gifted  girl  girlfriend  give  given  glad  glance  glass  global  glove  go  goal  God  gold  golden  golf  good  government  governor  grab  grade  gradually  graduate  grain  grand  grandfather  grandmother  grant  grass  grave  gray  great  greatest  green  grocery  ground  group  grow  growing  growth  guarantee  guard  guess  guest  guide  guideline  guilty  gun  guy  habit  habitat  hair  half  hall  hand  handful  handle  hang  happen  happy  hard  hardly  hat  hate  have  he  head  headline  headquarters  health  healthy  hear  hearing  heart  heat  heaven  heavily  heavy  heel  height  helicopter  hell  hello  help  helpful  her  here  heritage  hero  herself  hey  hi  hide  high  highlight  highly  highway  hill  him  himself  hip  hire  his  historian  historic  historical  history  hit  hold  hole  holiday  holy  home  homeless  honest  honey  honor  hope  horizon  horror  horse  hospital  host  hot  hotel  hour  house  household  housing  how  however  huge  human  humor  hundred  hungry  hunter  hunting  hurt  husband  hypothesis  I  ice  idea  ideal  identification  identify  identity  ie  if  ignore  ill  illegal  illness  illustrate  image  imagination  imagine  immediate  immediately  immigrant  immigration  impact  implement  implication  imply  importance  important  impose  impossible  impress  impression  impressive  improve  improvement  in  incentive  incident  include  including  income  incorporate  increase  increased  increasing  increasingly  incredible  indeed  independence  independent  index  Indian  indicate  indication  individual  industrial  industry  infant  infection  inflation  influence  inform  information  ingredient  initial  initially  initiative  injury  inner  innocent  inquiry  inside  insight  insist  inspire  install  instance  instead  institution  institutional  instruction  instructor  instrument  insurance  intellectual  intelligence  intend  intense  intensity  intention  interaction  interest  interested  interesting  internal  international  Internet  interpret  interpretation  intervention  interview  into  introduce  introduction  invasion  invest  investigate  investigation  investigator  investment  investor  invite  involve  involved  involvement  Iraqi  Irish  iron  Islamic  island  Israeli  issue  it  Italian  item  its  itself  jacket  jail  Japanese  jet  Jew  Jewish  job  join  joint  joke  journal  journalist  journey  joy  judge  judgment  juice  jump  junior  jury  just  justice  justify  keep  key  kick  kid  kill  killer  killing  kind  king  kiss  kitchen  knee  knife  knock  know  knowledge  lab  label  labor  laboratory  lack  lady  lake  land  landscape  language  lap  large  largely  last  late  later  Latin  latter  laugh  launch  law  lawn  lawsuit  lawyer  lay  layer  lead  leader  leadership  leading  leaf  league  lean  learn  learning  least  leather  leave  left  leg  legacy  legal  legend  legislation  legitimate  lemon  length  less  lesson  let  letter  level  liberal  library  license  lie  life  lifestyle  lifetime  lift  light  like  likely  limit  limitation  limited  line  link  lip  list  listen  literally  literary  literature  little  live  living  load  loan  local  locate  location  lock  long  long-term  look  loose  lose  loss  lost  lot  lots  loud  love  lovely  lover  low  lower  luck  lucky  lunch  lung  machine  mad  magazine  mail  main  mainly  maintain  maintenance  major  majority  make  maker  makeup  male  mall  man  manage  management  manager  manner  manufacturer  manufacturing  many  map  margin  mark  market  marketing  marriage  married  marry  mask  mass  massive  master  match  material  math  matter  may  maybe  mayor  me  meal  mean  meaning  meanwhile  measure  measurement  meat  mechanism  media  medical  medication  medicine  medium  meet  meeting  member  membership  memory  mental  mention  menu  mere  merely  mess  message  metal  meter  method  Mexican  middle  might  military  milk  million  mind  mine  minister  minor  minority  minute  miracle  mirror  miss  missile  mission  mistake  mix  mixture  mm-hmm  mode  model  moderate  modern  modest  mom  moment  money  monitor  month  mood  moon  moral  more  moreover  morning  mortgage  most  mostly  mother  motion  motivation  motor  mount  mountain  mouse  mouth  move  movement  movie  Mr  Mrs  Ms  much  multiple  murder  muscle  museum  music  musical  musician  Muslim  must  mutual  my  myself  mystery  myth  naked  name  narrative  narrow  nation  national  native  natural  naturally  nature  near  nearby  nearly  necessarily  necessary  neck  need  negative  negotiate  negotiation  neighbor  neighborhood  neither  nerve  nervous  net  network  never  nevertheless  new  newly  news  newspaper  next  nice  night  nine  no  nobody  nod  noise  nomination  none  nonetheless  nor  normal  normally  north  northern  nose  not  note  nothing  notice  notion  novel  now  nowhere  n't  nuclear  number  numerous  nurse  nut  object  objective  obligation  observation  observe  observer  obtain  obvious  obviously  occasion  occasionally  occupation  occupy  occur  ocean  odd  odds  of  off  offense  offensive  offer  office  officer  official  often  oh  oil  ok  okay  old  Olympic  on  once  one  ongoing  onion  online  only  onto  open  opening  operate  operating  operation  operator  opinion  opponent  opportunity  oppose  opposite  opposition  option  or  orange  order  ordinary  organic  organization  organize  orientation  origin  original  originally  other  others  otherwise  ought  our  ourselves  out  outcome  outside  oven  over  overall  overcome  overlook  owe  own  owner  pace  pack  package  page  pain  painful  paint  painter  painting  pair  pale  Palestinian  palm  pan  panel  pant  paper  parent  park  parking  part  participant  participate  participation  particular  particularly  partly  partner  partnership  party  pass  passage  passenger  passion  past  patch  path  patient  pattern  pause  pay  payment  PC  peace  peak  peer  penalty  people  pepper  per  perceive  percentage  perception  perfect  perfectly  perform  performance  perhaps  period  permanent  permission  permit  person  personal  personality  personally  personnel  perspective  persuade  pet  phase  phenomenon  philosophy  phone  photo  photograph  photographer  phrase  physical  physically  physician  piano  pick  picture  pie  piece  pile  pilot  pine  pink  pipe  pitch  place  plan  plane  planet  planning  plant  plastic  plate  platform  play  player  please  pleasure  plenty  plot  plus  PM  pocket  poem  poet  poetry  point  pole  police  policy  political  politically  politician  politics  poll  pollution  pool  poor  pop  popular  population  porch  port  portion  portrait  portray  pose  position  positive  possess  possibility  possible  possibly  post  pot  potato  potential  potentially  pound  pour  poverty  powder  power  powerful  practical  practice  pray  prayer  precisely  predict  prefer  preference  pregnancy  pregnant  preparation  prepare  prescription  presence  present  presentation  preserve  president  presidential  press  pressure  pretend  pretty  prevent  previous  previously  price  pride  priest  primarily  primary  prime  principal  principle  print  prior  priority  prison  prisoner  privacy  private  probably  problem  procedure  proceed  process  produce  producer  product  production  profession  professional  professor  profile  profit  program  progress  project  prominent  promise  promote  prompt  proof  proper  properly  property  proportion  proposal  propose  proposed  prosecutor  prospect  protect  protection  protein  protest  proud  prove  provide  provider  province  provision  psychological  psychologist  psychology  public  publication  publicly  publish  publisher  pull  punishment  purchase  pure  purpose  pursue  push  put  qualify  quality  quarter  quarterback  question  quick  quickly  quiet  quietly  quit  quite  quote  race  racial  radical  radio  rail  rain  raise  range  rank  rapid  rapidly  rare  rarely  rate  rather  rating  ratio  raw  reach  react  reaction  read  reader  reading  ready  real  reality  realize  really  reason  reasonable  recall  receive  recent  recently  recipe  recognition  recognize  recommend  recommendation  record  recording  recover  recovery  recruit  red  reduce  reduction  refer  reference  reflect  reflection  reform  refugee  refuse  regard  regarding  regardless  regime  region  regional  register  regular  regularly  regulate  regulation  reinforce  reject  relate  relation  relationship  relative  relatively  relax  release  relevant  relief  religion  religious  rely  remain  remaining  remarkable  remember  remind  remote  remove  repeat  repeatedly  replace  reply  report  reporter  represent  representation  representative  Republican  reputation  request  require  requirement  research  researcher  resemble  reservation  resident  resist  resistance  resolution  resolve  resort  resource  respect  respond  respondent  response  responsibility  responsible  rest  restaurant  restore  restriction  result  retain  retire  retirement  return  reveal  revenue  review  revolution  rhythm  rice  rich  rid  ride  rifle  right  ring  rise  risk  river  road  rock  role  roll  romantic  roof  room  root  rope  rose  rough  roughly  round  route  routine  row  rub  rule  run  running  rural  rush  Russian  sacred  sad  safe  safety  sake  salad  salary  sale  sales  salt  same  sample  sanction  sand  satellite  satisfaction  satisfy  sauce  save  saving  say  scale  scandal  scared  scenario  scene  schedule  scheme  scholar  scholarship  school  science  scientific  scientist  scope  score  scream  screen  script  sea  search  season  seat  second  secret  secretary  section  sector  secure  security  see  seed  seek  seem  segment  seize  select  selection  self  sell  Senate  senator  send  senior  sense  sensitive  sentence  separate  sequence  series  serious  seriously  serve  service  session  set  setting  settle  settlement  seven  several  severe  sex  sexual  shade  shadow  shake  shall  shape  share  sharp  she  sheet  shelf  shell  shelter  shift  shine  ship  shirt  shit  shock  shoe  shoot  shooting  shop  shopping  shore  short  shortly  shot  should  shoulder  shout  show  shower  shrug  shut  sick  side  sigh  sight  sign  signal  significance  significant  significantly  silence  silent  silver  similar  similarly  simple  simply  sin  since  sing  singer  single  sink  sir  sister  sit  site  situation  six  size  ski  skill  skin  sky  slave  sleep  slice  slide  slight  slightly  slip  slow  slowly  small  smart  smell  smile  smoke  smooth  snap  snow  so  so-called  soccer  social  society  soft  software  soil  solar  soldier  solid  solution  solve  some  somebody  somehow  someone  something  sometimes  somewhat  somewhere  son  song  soon  sophisticated  sorry  sort  soul  sound  soup  source  south  southern  Soviet  space  Spanish  speak  speaker  special  specialist  species  specific  specifically  speech  speed  spend  spending  spin  spirit  spiritual  split  spokesman  sport  spot  spread  spring  square  squeeze  stability  stable  staff  stage  stair  stake  stand  standard  standing  star  stare  start  state  statement  station  statistics  status  stay  steady  steal  steel  step  stick  still  stir  stock  stomach  stone  stop  storage  store  storm  story  straight  strange  stranger  strategic  strategy  stream  street  strength  strengthen  stress  stretch  strike  string  strip  stroke  strong  strongly  structure  struggle  student  studio  study  stuff  stupid  style  subject  submit  subsequent  substance  substantial  succeed  success  successful  successfully  such  sudden  suddenly  sue  suffer  sufficient  sugar  suggest  suggestion  suicide  suit  summer  summit  sun  super  supply  support  supporter  suppose  supposed  Supreme  sure  surely  surface  surgery  surprise  surprised  surprising  surprisingly  surround  survey  survival  survive  survivor  suspect  sustain  swear  sweep  sweet  swim  swing  switch  symbol  symptom  system  table  tablespoon  tactic  tail  take  tale  talent  talk  tall  tank  tap  tape  target  task  taste  tax  taxpayer  tea  teach  teacher  teaching  team  tear  teaspoon  technical  technique  technology  teen  teenager  telephone  telescope  television  tell  temperature  temporary  ten  tend  tendency  tennis  tension  tent  term  terms  terrible  territory  terror  terrorism  terrorist  test  testify  testimony  testing  text  than  thank  thanks  that  the  theater  their  them  theme  themselves  then  theory  therapy  there  therefore  these  they  thick  thin  thing  think  thinking  third  thirty  this  those  though  thought  thousand  threat  threaten  three  throat  through  throughout  throw  thus  ticket  tie  tight  time  tiny  tip  tire  tired  tissue  title  to  tobacco  today  toe  together  tomato  tomorrow  tone  tongue  tonight  too  tool  tooth  top  topic  toss  total  totally  touch  tough  tour  tourist  tournament  toward  towards  tower  town  toy  trace  track  trade  tradition  traditional  traffic  tragedy  trail  train  training  transfer  transform  transformation  transition  translate  transportation  travel  treat  treatment  treaty  tree  tremendous  trend  trial  tribe  trick  trip  troop  trouble  truck  true  truly  trust  truth  try  tube  tunnel  turn  TV  twelve  twenty  twice  twin  two  type  typical  typically  ugly  ultimate  ultimately  unable  uncle  under  undergo  understand  understanding  unfortunately  uniform  union  unique  unit  United  universal  universe  university  unknown  unless  unlike  unlikely  until  unusual  up  upon  upper  urban  urge  us  use  used  useful  user  usual  usually  utility  vacation  valley  valuable  value  variable  variation  variety  various  vary  vast  vegetable  vehicle  venture  version  versus  very  vessel  veteran  via  victim  victory  video  view  viewer  village  violate  violation  violence  violent  virtually  virtue  virus  visible  vision  visit  visitor  visual  vital  voice  volume  volunteer  vote  voter  vs  vulnerable  wage  wait  wake  walk  wall  wander  want  war  warm  warn  warning  wash  waste  watch  water  wave  way  we  weak  wealth  wealthy  weapon  wear  weather  wedding  week  weekend  weekly  weigh  weight  welcome  welfare  well  west  western  wet  what  whatever  wheel  when  whenever  where  whereas  whether  which  while  whisper  white  who  whole  whom  whose  why  wide  widely  widespread  wife  wild  will  willing  win  wind  window  wine  wing  winner  winter  wipe  wire  wisdom  wise  wish  with  withdraw  within  without  witness  woman  wonder  wonderful  wood  wooden  word  work  worker  working  works  workshop  world  worried  worry  worth  would  wound  wrap  write  writer  writing  wrong  yard  yeah  year  yell  yellow  yes  yesterday  yet  yield  you  young  your  yours  yourself  youth  zone";
        String[] allWords = (morseText+word_3000).split(" ");
        for (String allWord : allWords) {
            StringBuilder word = new StringBuilder();
            String temp = allWord.toUpperCase();
            if (temp.length() < 10) {
                for (int j = 0; j < temp.length(); j++) {
                    char ch = temp.charAt(j);
                    if (ch >= 'A' && ch <= 'Z') {
                        word.append(ch);
                    }
                }
                String newWorld = word.toString();
                if (!wordList.contains(newWorld)) {
                    wordList.add(newWorld);
                }
            }
        }

        buttonNext.setOnClickListener(v -> randomWord(savedInstanceState));
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        randomWord(savedInstanceState);

    }

    protected void onCorrectAnswer() {
        try {
            if (!mediaPlayerPoint.isPlaying()) {
                mediaPlayerPoint.seekTo(0);
                mediaPlayerPoint.start();
            }
        } catch (Exception e) {
            //ignore error
        }
    }

    protected void onErrorAnswer() {

    }

    protected void randomWord(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            int index = random.nextInt(1000) % wordList.size();
            currentWord = wordList.get(index);
            currentWorldIndex = 0;
            totalWords += 1;
            responseWord = "";

        } else {
            currentWord = savedInstanceState.getString(STATE_CURRENT_WORD);
            responseWord = savedInstanceState.getString(STATE_RESPONSE_WORD);
            totalCorrect = savedInstanceState.getInt(STATE_TOTAL_CORRECT);
            totalWords = savedInstanceState.getInt(STATE_TOTAL_WORDS);
            currentWorldIndex = savedInstanceState.getInt(STATE_RESPONSE_WORD_INDEX);
        }
        textViewWord.setTextColor(primaryLetterColor);
        textViewWord.setText(currentWord);
        textViewResponse.setText(responseWord);

    }
}
