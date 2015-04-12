package za.co.discoverylife.st2git.filter;

import za.co.discoverylife.st2git.Reference;

public class FilterLife extends BaseFilter
{
  private static final String[] PROJECTS = new String[] {
      "BaseFramework",
      "Domain","IntegrationServices","JBossManagement","LimitedInfoEntities","SpreadSheetExecutorServices",
      "FI_HomeLoan","FI_OnlineCreditControlIntermediaryFeedback","FI_SMS","FI_values","GUIFramework",
      "GenericProxy","IN_BrokerValidation","IN_NotificationControl","LifeCommon","NB_Corporate_Workflow",
      "PolicyFacts","Reports","RulesEngineAdministration","SC_CommsMessaging","SystemSecurity",
      "AggregatedReward","FI_SimpleTaxCalculator","FI_UserSecurity","FaceCase","GenericSMS",
      "IN_ActivationSMS","IN_DocumentRouter","IN_Workflow","NB_SearchLOA","QueueManagement",
      "RulesEngineServices","SC_SERVICING_WORKBENCH","sc_commissions",
      "FinanceGenerics","LifeFaceCase","RulesEngine",
      "SC_Entities",
      "Astute","DocumentAdministration",
      "PolicyAdministration",
      "FraudServices","HPPConversion","HealthAndVitalityServices","HealthQuoteLinkUpload","IN_Inbox",
      "INetService","IntermediaryServices","ReassuranceAdministration","SC_FranchiseWorkbench",
      "MovementAdministration","SC_Reassurance_2","ScheduledProcesses",
      "BalanceOfPayment","BusinessEvent","CardServices","FinanceIntegrationServices","FinanceTransactionProcessing",
      "HealthUpdate","IN_MedicalPayments","RPAR","SC_EntitiesMerge",
      "BulkQuotes","FinanceBusinessAdmin","FinancialAdvisorZone","FundManagement","IN_SmartServices",
      "SC_SAMOL",
      "BusinessConservation","FinanceDashboard","RequirementsAdministration","SC_ContractualProcess","SC_SAM",
      "ClaimAdministration",
      "ClaimWorkbench","ClaimYearEnd","NB_Underwriting_2",
      "LifeWeb","NB_QuoteLinkUpload","SC_OnlineQuote",
      //"?"
  };

  public boolean check(Reference reference)
  {
    include = false;
    fixTimeStamp(reference);
    if (reference.stLabel.startsWith("IB_Black"))
    {
      include = true;
    }
    if (include)
    {
      reference.repo = getRepoName();
      reference.key = reference.stLabel;
      reference.gitBranch = "master";
      reference.gitLabel = reference.stLabel;
    }
    return include;
  }

  public String getRepoName()
  {
    return LIFE;
  }

  public boolean include(String projectName)
  {
    return isIn(projectName, PROJECTS);
  }
}
