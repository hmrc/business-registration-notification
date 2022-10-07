
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.SbtBobbyPlugin.BobbyKeys.bobbyRulesURL
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName: String = "business-registration-notification"

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;view.*;config.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimumStmtTotal := 90,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(PlayScala, SbtDistributablesPlugin): _*)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(PlayKeys.playDefaultPort := 9661)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings())
  .settings(majorVersion := 1)
  .settings(
    scalacOptions += "-Xlint:-unused",
    scalaVersion := "2.12.15",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    resolvers += Resolver.jcenterRepo,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(bobbyRulesURL := Some(new URL("https://webstore.tax.service.gov.uk/bobby-config/deprecated-dependencies.json")))
