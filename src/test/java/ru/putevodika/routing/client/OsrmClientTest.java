package ru.putevodika.routing.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class OsrmClientTest {

    private MockRestServiceServer server;

    private OsrmClient osrmClient;


    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://osrm.test");

        server = MockRestServiceServer
                .bindTo(builder)
                .build();

        osrmClient = new OsrmClient(
                builder.build()
        );
    }


    @Test
    void shouldBuildWalkingRoute() {

        String response = """
                {
                  "code": "Ok",
                  "routes": [
                    {
                      "distance": 1334.2,
                      "duration": 960.8,
                      "geometry": {
                        "type": "LineString",
                        "coordinates": [
                          [48.398176, 54.314629],
                          [48.410136, 54.319951]
                        ]
                      }
                    }
                  ]
                }
                """;

        server.expect(
                        once(),
                        requestTo(
                                containsString(
                                        "/route/v1/foot/"
                                )
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withSuccess(
                                response,
                                MediaType.APPLICATION_JSON
                        )
                );


        OsrmRouteResult result =
                osrmClient.buildWalkingRoute(
                        54.3142,
                        48.3978,
                        54.3200,
                        48.4100
                );


        assertThat(result.distanceMeters())
                .isEqualTo(1334.2);

        assertThat(result.durationSeconds())
                .isEqualTo(960.8);

        assertThat(result.geometryType())
                .isEqualTo("LineString");

        assertThat(result.coordinates())
                .hasSize(2);

        assertThat(
                result.coordinates().getFirst()
        ).containsExactly(
                48.398176,
                54.314629
        );


        server.verify();
    }


    @Test
    void shouldBuildWalkingMatrix() {

        String response = """
                {
                  "code": "Ok",
                  "durations": [
                    [0.0, 960.8, 1500.0],
                    [960.8, 0.0, 700.0],
                    [1500.0, 700.0, 0.0]
                  ],
                  "distances": [
                    [0.0, 1334.2, 2100.0],
                    [1334.2, 0.0, 950.0],
                    [2100.0, 950.0, 0.0]
                  ]
                }
                """;

        server.expect(
                        once(),
                        requestTo(
                                containsString(
                                        "/table/v1/foot/"
                                )
                        )
                )
                .andExpect(method(GET))
                .andRespond(
                        withSuccess(
                                response,
                                MediaType.APPLICATION_JSON
                        )
                );


        OsrmMatrixResult result =
                osrmClient.buildWalkingMatrix(
                        List.of(
                                new RoutingPoint(
                                        54.3142,
                                        48.3978
                                ),
                                new RoutingPoint(
                                        54.3200,
                                        48.4100
                                ),
                                new RoutingPoint(
                                        54.3250,
                                        48.4200
                                )
                        )
                );


        assertThat(
                result.distancesMeters()
        ).hasSize(3);

        assertThat(
                result.durationsSeconds()
        ).hasSize(3);


        assertThat(
                result.distancesMeters()
                        .getFirst()
                        .get(1)
        ).isEqualTo(
                1334.2
        );

        assertThat(
                result.durationsSeconds()
                        .getFirst()
                        .get(1)
        ).isEqualTo(
                960.8
        );


        server.verify();
    }
}