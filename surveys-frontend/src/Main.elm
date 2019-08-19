module Main exposing (main)

import Browser
import Html exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (Decoder)


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = \_ -> Sub.none
        }


type alias Model =
    { survey : Survey
    }


init : () -> ( Model, Cmd Msg )
init _ =
    ( { survey = { surveyId = "", startDateTime = "", endDateTime = "" } }
    , Cmd.none
    )


type Msg
    = Click
    | GotSurvey (Result Http.Error Survey)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Click ->
            ( model
            , Http.get
                { url = "/surveys/01DJ9KW69W1059TF3WE5GJTEDM"
                , expect = Http.expectJson GotSurvey surveyDecoder
                }
            )

        GotSurvey (Ok survey) ->
            ( { model | survey = survey }, Cmd.none )

        GotSurvey (Err error) ->
            ( { model | survey = { surveyId = Debug.toString error, startDateTime = "", endDateTime = "" } }, Cmd.none )


view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Click ] [ text "Get" ]
        , table []
            [ tr []
                [ th [] [ text "Survey ID" ]
                , td [] [ text model.survey.surveyId ]
                ]
            , tr []
                [ th [] [ text "Start Date Time" ]
                , td [] [ text model.survey.startDateTime ]
                ]
            , tr []
                [ th [] [ text "End Date Time" ]
                , td [] [ text model.survey.endDateTime ]
                ]
            ]
        ]


type alias Survey =
    { surveyId : String
    , startDateTime : String
    , endDateTime : String
    }


surveyDecoder : Decoder Survey
surveyDecoder =
    Json.Decode.map3 Survey
        (Json.Decode.field "surveyId" Json.Decode.string)
        (Json.Decode.field "startDateTime" Json.Decode.string)
        (Json.Decode.field "endDateTime" Json.Decode.string)
