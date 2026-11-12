const root = "rexenvoys";

export const rexenvoys = {
  label: "RexEnvoys",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Messages", slug: `${root}/configuration/messages` },
      ],
    },
    {
      label: "Guides",
      items: [
        { label: "Events", slug: `${root}/guides/events` },
        { label: "Flares", slug: `${root}/guides/flares` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
        { label: "Placeholders", slug: `${root}/reference/placeholders` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
